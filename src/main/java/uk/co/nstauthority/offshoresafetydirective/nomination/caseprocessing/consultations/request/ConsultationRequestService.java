package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@Service
class ConsultationRequestService {

  private final CaseEventService caseEventService;

  private final ConsultationRequestEventPublisher consultationRequestEventPublisher;

  @Autowired
  ConsultationRequestService(CaseEventService caseEventService,
                             ConsultationRequestEventPublisher consultationRequestEventPublisher) {
    this.caseEventService = caseEventService;
    this.consultationRequestEventPublisher = consultationRequestEventPublisher;
  }

  @Transactional
  public void requestConsultation(NominationDetail nominationDetail) {
    caseEventService.createSentForConsultationEvent(nominationDetail);
    consultationRequestEventPublisher.publish(new NominationId(nominationDetail.getNomination().getId()));
  }
}
