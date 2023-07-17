package uk.co.nstauthority.offshoresafetydirective.nomination;


import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;

@Service
public class NominationDetailService {

  private final NominationService nominationService;
  private final NominationDetailRepository nominationDetailRepository;
  private final NominationReferenceService nominationReferenceService;

  private final NominationSubmittedEventPublisher nominationSubmittedEventPublisher;
  private final CaseEventService caseEventService;
  private final Clock clock;

  @Autowired
  public NominationDetailService(
      NominationService nominationService,
      NominationDetailRepository nominationDetailRepository,
      NominationReferenceService nominationReferenceService,
      NominationSubmittedEventPublisher nominationSubmittedEventPublisher, CaseEventService caseEventService,
      Clock clock) {
    this.nominationService = nominationService;
    this.nominationDetailRepository = nominationDetailRepository;
    this.nominationReferenceService = nominationReferenceService;
    this.nominationSubmittedEventPublisher = nominationSubmittedEventPublisher;
    this.caseEventService = caseEventService;
    this.clock = clock;
  }

  @Transactional
  public void submitNomination(NominationDetail nominationDetail) {
    nominationDetail.setStatus(NominationStatus.SUBMITTED);
    nominationDetail.setSubmittedInstant(clock.instant());
    nominationDetailRepository.save(nominationDetail);
    if (nominationDetail.getVersion().equals(1)) {
      nominationReferenceService.setNominationReference(nominationDetail);
    }
    caseEventService.createSubmissionEvent(nominationDetail);
    nominationSubmittedEventPublisher.publishNominationSubmittedEvent(nominationDetail);
  }

  public NominationDetail getLatestNominationDetail(NominationId nominationId) {
    var nomination = nominationService.getNominationByIdOrError(nominationId);
    return nominationDetailRepository.findFirstByNominationOrderByVersionDesc(nomination)
        .orElseThrow(() -> new OsdEntityNotFoundException(String.format(
            "Cannot find latest NominationDetail with ID: %s",
            nomination.getId()
        )));
  }

  public Optional<NominationDetail> getNominationDetailWithVersion(Nomination nomination, int version) {
    return nominationDetailRepository.findFirstByNominationAndVersion(nomination, version);
  }

  public Optional<NominationDetail> getLatestNominationDetailOptional(NominationId nominationId) {
    return nominationDetailRepository.findFirstByNomination_IdOrderByVersionDesc(nominationId.id());
  }

  public Optional<NominationDetail> getLatestNominationDetailWithStatuses(NominationId nominationId,
                                                                          Collection<NominationStatus> nominationStatuses) {
    return nominationDetailRepository.findFirstByNomination_IdAndStatusInOrderByVersionDesc(
        nominationId.id(),
        nominationStatuses
    );
  }

  public Optional<NominationDetail> getVersionedNominationDetailWithStatuses(NominationId nominationId,
                                                                             Integer version,
                                                                             Collection<NominationStatus> nominationStatuses) {
    return nominationDetailRepository.findFirstByNomination_IdAndVersionAndStatusInOrderByVersionDesc(
        nominationId.id(),
        version,
        nominationStatuses
    );
  }

  public Optional<NominationDetail> getNominationDetail(NominationDetailId nominationDetailId) {
    return nominationDetailRepository.findById(nominationDetailId.id());
  }

  public List<NominationDetailDto> getPostSubmissionNominationDetailDtos(Nomination nomination) {
    return nominationDetailRepository.findAllByNominationAndStatusIn(
            nomination,
            NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
        )
        .stream()
        .map(NominationDetailDto::fromNominationDetail)
        .toList();
  }

  @Transactional
  public void deleteNominationDetail(NominationDetail nominationDetail) {
    if (nominationDetail.getStatus() == NominationStatus.DRAFT) {
      nominationDetail.setStatus(NominationStatus.DELETED);
      nominationDetail.setVersion(null);
      nominationDetailRepository.save(nominationDetail);
    } else {
      throw new IllegalArgumentException("Cannot delete NominationDetail [%d] as NominationStatus is not %s"
          .formatted(nominationDetail.getId(), NominationStatus.DRAFT));
    }
  }

  @Transactional
  public void updateNominationDetailStatusByDecision(NominationDetail nominationDetail,
                                                     @NotNull NominationDecision nominationDecision) {
    if (nominationDetail.getStatus() != NominationStatus.SUBMITTED) {
      throw new IllegalArgumentException("Cannot set decision for NominationDetail [%d] as NominationStatus is not %s"
          .formatted(nominationDetail.getId(), NominationStatus.SUBMITTED));
    }
    if (nominationDecision.equals(NominationDecision.OBJECTION)) {
      nominationDetail.setStatus(NominationStatus.OBJECTED);
    } else {
      nominationDetail.setStatus(NominationStatus.AWAITING_CONFIRMATION);
    }
    nominationDetailRepository.save(nominationDetail);
  }

  @Transactional
  public void withdrawNominationDetail(NominationDetail nominationDetailToWithdraw) {

    var allowedStatuses = EnumSet.of(
        NominationStatus.SUBMITTED,
        NominationStatus.AWAITING_CONFIRMATION
    );

    if (!allowedStatuses.contains(nominationDetailToWithdraw.getStatus())) {
      var statuses = allowedStatuses.stream()
          .map(Enum::name)
          .collect(Collectors.joining(","));
      throw new IllegalArgumentException("Cannot withdrawn NominationDetail [%d] as NominationStatus is not one of [%s]"
          .formatted(nominationDetailToWithdraw.getId(), statuses));
    }

    nominationDetailToWithdraw.setStatus(NominationStatus.WITHDRAWN);

    // check for any draft updates and ensure they are delete as well
    Optional<NominationDetail> draftNominationDetailUpdate = nominationDetailRepository
        .findFirstByNomination_IdAndStatusInOrderByVersionDesc(
            nominationDetailToWithdraw.getNomination().getId(),
            Collections.singletonList(NominationStatus.DRAFT)
        );

    draftNominationDetailUpdate.ifPresent(this::deleteNominationDetail);

    nominationDetailRepository.save(nominationDetailToWithdraw);
  }

  public Set<NominationDto> getNominationsByReferenceLikeWithStatuses(String reference,
                                                                      Collection<NominationStatus> statuses) {
    return nominationDetailRepository.getNominationDetailsByStatusInAndNomination_ReferenceContainsIgnoreCase(
            statuses,
            reference
        )
        .stream()
        .map(NominationDetailDto::fromNominationDetail)
        .map(NominationDetailDto::nominationDto)
        .collect(Collectors.toSet());
  }

}
