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

  private final NominationSubmittedEventPublisher nominationSubmittedEventPublisher;
  private final Clock clock;

  @Autowired
  public NominationDetailService(
      NominationService nominationService,
      NominationDetailRepository nominationDetailRepository,
      NominationSubmittedEventPublisher nominationSubmittedEventPublisher, Clock clock) {
    this.nominationService = nominationService;
    this.nominationDetailRepository = nominationDetailRepository;
    this.nominationSubmittedEventPublisher = nominationSubmittedEventPublisher;
    this.clock = clock;
  }

  @Transactional
  public void submitNomination(NominationDetail nominationDetail) {
    nominationDetail.setStatus(NominationStatus.SUBMITTED);
    nominationDetail.setSubmittedInstant(clock.instant());
    nominationDetailRepository.save(nominationDetail);
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
}
