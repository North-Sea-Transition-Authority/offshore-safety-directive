package uk.co.nstauthority.offshoresafetydirective.nomination;


import java.time.Clock;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;

@Service
public class NominationDetailService {

  private final NominationService nominationService;
  private final NominationDetailRepository nominationDetailRepository;
  private final NominationReferenceService nominationReferenceService;

  private final NominationSubmittedEventPublisher nominationSubmittedEventPublisher;
  private final Clock clock;

  @Autowired
  public NominationDetailService(
      NominationService nominationService,
      NominationDetailRepository nominationDetailRepository,
      NominationReferenceService nominationReferenceService,
      NominationSubmittedEventPublisher nominationSubmittedEventPublisher, Clock clock) {
    this.nominationService = nominationService;
    this.nominationDetailRepository = nominationDetailRepository;
    this.nominationReferenceService = nominationReferenceService;
    this.nominationSubmittedEventPublisher = nominationSubmittedEventPublisher;
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
    nominationSubmittedEventPublisher.publishNominationSubmittedEvent(nominationDetail);
  }

  public NominationDetail getLatestNominationDetail(NominationId nominationId) {
    var nomination = nominationService.getNominationByIdOrError(nominationId);
    return nominationDetailRepository.findFirstByNominationOrderByVersionDesc(nomination)
        .orElseThrow(() -> {
          throw new OsdEntityNotFoundException(String.format(
              "Cannot find latest NominationDetail with ID: %s",
              nomination.getId()
          ));
        });
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

  @Transactional
  public void deleteNominationDetail(NominationDetail nominationDetail) {
    if (nominationDetail.getStatus() == NominationStatus.DRAFT) {
      nominationDetail.setStatus(NominationStatus.DELETED);
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
      nominationDetail.setStatus(NominationStatus.CLOSED);
    } else {
      nominationDetail.setStatus(NominationStatus.AWAITING_CONFIRMATION);
    }
    nominationDetailRepository.save(nominationDetail);
  }

  @Transactional
  public void withdrawNominationDetail(NominationDetail nominationDetail) {
    var allowedStatuses = EnumSet.of(
        NominationStatus.SUBMITTED,
        NominationStatus.AWAITING_CONFIRMATION
    );

    if (!allowedStatuses.contains(nominationDetail.getStatus())) {
      var statuses = allowedStatuses.stream()
          .map(Enum::name)
          .collect(Collectors.joining(","));
      throw new IllegalArgumentException("Cannot withdrawn NominationDetail [%d] as NominationStatus is not one of [%s]"
          .formatted(nominationDetail.getId(), statuses));
    }

    nominationDetail.setStatus(NominationStatus.WITHDRAWN);
    nominationDetailRepository.save(nominationDetail);
  }

}
