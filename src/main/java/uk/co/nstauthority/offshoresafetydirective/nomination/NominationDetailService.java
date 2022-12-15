package uk.co.nstauthority.offshoresafetydirective.nomination;


import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;

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

}
