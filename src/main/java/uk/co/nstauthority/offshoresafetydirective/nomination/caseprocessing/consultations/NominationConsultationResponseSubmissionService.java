package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@Service
class NominationConsultationResponseSubmissionService {

  private final FileUploadService fileUploadService;
  private final UploadedFileDetailService uploadedFileDetailService;

  private final CaseEventService caseEventService;

  @Autowired
  NominationConsultationResponseSubmissionService(FileUploadService fileUploadService,
                                                  UploadedFileDetailService uploadedFileDetailService,
                                                  CaseEventService caseEventService) {
    this.fileUploadService = fileUploadService;
    this.uploadedFileDetailService = uploadedFileDetailService;
    this.caseEventService = caseEventService;
  }

  @Transactional
  public void submitConsultationResponse(NominationDetail nominationDetail, NominationConsultationResponseForm form) {
    fileUploadService.updateFileUploadDescriptions(form.getConsultationResponseFiles());

    uploadedFileDetailService.submitFiles(form.getConsultationResponseFiles());

    caseEventService.createConsultationResponseEvent(
        nominationDetail,
        form.getResponse().getInputValue(),
        form.getConsultationResponseFiles()
    );
  }

}
