package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.UploadedFileDetailService;

@Service
class GeneralCaseNoteSubmissionService {

  private final CaseEventService caseEventService;
  private final FileUploadService fileUploadService;
  private final UploadedFileDetailService uploadedFileDetailService;

  @Autowired
  GeneralCaseNoteSubmissionService(CaseEventService caseEventService,
                                   FileUploadService fileUploadService,
                                   UploadedFileDetailService uploadedFileDetailService) {
    this.caseEventService = caseEventService;
    this.fileUploadService = fileUploadService;
    this.uploadedFileDetailService = uploadedFileDetailService;
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
    uploadedFileDetailService.submitFiles(form.getCaseNoteFiles());
  }

}
