package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.FilePurpose;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.UploadedFileDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.reference.NominationDetailFileReference;

@Service
class NomineeDetailFormService {

  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private final NomineeDetailFormValidator nomineeDetailFormValidator;
  private final UploadedFileDetailService uploadedFileDetailService;
  private final FileUploadService fileUploadService;

  @Autowired
  NomineeDetailFormService(NomineeDetailPersistenceService nomineeDetailPersistenceService,
                           NomineeDetailFormValidator nomineeDetailFormValidator,
                           UploadedFileDetailService uploadedFileDetailService, FileUploadService fileUploadService) {
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.nomineeDetailFormValidator = nomineeDetailFormValidator;
    this.uploadedFileDetailService = uploadedFileDetailService;
    this.fileUploadService = fileUploadService;
  }

  NomineeDetailForm getForm(NominationDetail nominationDetail) {
    return nomineeDetailPersistenceService.getNomineeDetail(nominationDetail)
        .map(this::nomineeDetailEntityToForm)
        .orElseGet(NomineeDetailForm::new);
  }

  BindingResult validate(NomineeDetailForm form, BindingResult bindingResult) {
    nomineeDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private NomineeDetailForm nomineeDetailEntityToForm(NomineeDetail entity) {
    var form = new NomineeDetailForm();
    form.setNominatedOrganisationId(entity.getNominatedOrganisationId());
    form.setReasonForNomination(entity.getReasonForNomination());
    form.setPlannedStartDay(String.valueOf(entity.getPlannedStartDate().getDayOfMonth()));
    form.setPlannedStartMonth(String.valueOf(entity.getPlannedStartDate().getMonthValue()));
    form.setPlannedStartYear(String.valueOf(entity.getPlannedStartDate().getYear()));
    form.setOperatorHasAuthority(entity.getOperatorHasAuthority());
    form.setOperatorHasCapacity(entity.getOperatorHasCapacity());
    form.setLicenseeAcknowledgeOperatorRequirements(entity.getLicenseeAcknowledgeOperatorRequirements());

    Map<FilePurpose, List<UploadedFileView>> viewMap =
        uploadedFileDetailService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
            new NominationDetailFileReference(entity.getNominationDetail()),
            List.of(NomineeDetailAppendixFileController.PURPOSE.purpose())
        );
    var appendixFileViews = viewMap.getOrDefault(NomineeDetailAppendixFileController.PURPOSE, List.of());
    form.setAppendixDocuments(fileUploadService.getFileUploadFormsFromUploadedFileViews(appendixFileViews));

    return form;
  }
}
