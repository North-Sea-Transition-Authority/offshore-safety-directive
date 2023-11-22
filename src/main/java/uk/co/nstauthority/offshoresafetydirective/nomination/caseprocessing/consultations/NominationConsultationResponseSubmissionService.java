package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@Service
class NominationConsultationResponseSubmissionService {

  private final FileUploadService fileUploadService;
  private final FileAssociationService fileAssociationService;

  private final CaseEventService caseEventService;

  @Autowired
  NominationConsultationResponseSubmissionService(FileUploadService fileUploadService,
                                                  FileAssociationService fileAssociationService,
                                                  CaseEventService caseEventService) {
    this.fileUploadService = fileUploadService;
    this.fileAssociationService = fileAssociationService;
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
