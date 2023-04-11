package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.NominationDetailFileService;

@Service
class NominationConsultationResponseSubmissionService {

  private final FileUploadService fileUploadService;
  private final NominationDetailFileService nominationDetailFileService;

  @Autowired
  NominationConsultationResponseSubmissionService(FileUploadService fileUploadService,
                                                  NominationDetailFileService nominationDetailFileService) {
    this.fileUploadService = fileUploadService;
    this.nominationDetailFileService = nominationDetailFileService;
  }

  @Transactional
  public void submitConsultationResponse(NominationDetail nominationDetail, NominationConsultationResponseForm form) {
    fileUploadService.updateFileUploadDescriptions(form.getConsultationResponseFiles());

    nominationDetailFileService.submitAndCleanFiles(nominationDetail, form.getConsultationResponseFiles(),
        NominationConsultationResponseFileController.VIRTUAL_FOLDER);
  }

}
