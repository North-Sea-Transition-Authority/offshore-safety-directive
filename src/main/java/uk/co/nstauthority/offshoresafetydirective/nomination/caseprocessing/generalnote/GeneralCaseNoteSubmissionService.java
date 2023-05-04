package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@Service
class GeneralCaseNoteSubmissionService {

  private final CaseEventService caseEventService;
  private final FileUploadService fileUploadService;
  private final FileAssociationService fileAssociationService;

  @Autowired
  GeneralCaseNoteSubmissionService(CaseEventService caseEventService,
                                   FileUploadService fileUploadService,
                                   FileAssociationService fileAssociationService) {
    this.caseEventService = caseEventService;
    this.fileUploadService = fileUploadService;
    this.fileAssociationService = fileAssociationService;
  }

  @Transactional
  public void submitCaseNote(NominationDetail nominationDetail, GeneralCaseNoteForm form) {
    caseEventService.createGeneralCaseNoteEvent(
        nominationDetail,
        form.getCaseNoteSubject().getInputValue(),
        form.getCaseNoteText().getInputValue(),
        form.getCaseNoteFiles()
    );

    fileUploadService.updateFileUploadDescriptions(form.getCaseNoteFiles());
    fileAssociationService.submitFiles(form.getCaseNoteFiles());
  }

}
