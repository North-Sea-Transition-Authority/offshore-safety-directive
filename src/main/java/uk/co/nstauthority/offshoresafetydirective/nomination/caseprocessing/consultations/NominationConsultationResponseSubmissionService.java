package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@Service
class NominationConsultationResponseSubmissionService {

  private final CaseEventService caseEventService;

  @Autowired
  NominationConsultationResponseSubmissionService(CaseEventService caseEventService) {
    this.caseEventService = caseEventService;
  }

  @Transactional
  public void submitConsultationResponse(NominationDetail nominationDetail, NominationConsultationResponseForm form) {
    caseEventService.createConsultationResponseEvent(
        nominationDetail,
        form.getResponse().getInputValue(),
        form.getConsultationResponseFiles()
    );
  }

}
