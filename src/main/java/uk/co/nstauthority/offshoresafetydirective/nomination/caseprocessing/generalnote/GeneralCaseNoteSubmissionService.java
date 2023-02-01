package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@Service
class GeneralCaseNoteSubmissionService {

  private final CaseEventService caseEventService;

  @Autowired
  GeneralCaseNoteSubmissionService(CaseEventService caseEventService) {
    this.caseEventService = caseEventService;
  }

  @Transactional
  public void submitCaseNote(NominationDetail nominationDetail, GeneralCaseNoteForm form) {
    caseEventService.createGeneralCaseNoteEvent(nominationDetail, form.getCaseNoteSubject().getInputValue(),
        form.getCaseNoteText().getInputValue());
  }

}
