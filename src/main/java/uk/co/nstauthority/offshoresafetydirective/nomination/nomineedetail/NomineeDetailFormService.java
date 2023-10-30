package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FilePurpose;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;

@Service
class NomineeDetailFormService {

  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private final NomineeDetailFormValidator nomineeDetailFormValidator;
  private final FileAssociationService fileAssociationService;
  private final FileUploadService fileUploadService;

  @Autowired
  NomineeDetailFormService(NomineeDetailPersistenceService nomineeDetailPersistenceService,
                           NomineeDetailFormValidator nomineeDetailFormValidator,
                           FileAssociationService fileAssociationService, FileUploadService fileUploadService) {
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.nomineeDetailFormValidator = nomineeDetailFormValidator;
    this.fileAssociationService = fileAssociationService;
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
    form.setOperatorHasAuthority(Objects.toString(entity.getOperatorHasAuthority(), null));
    form.setOperatorHasCapacity(Objects.toString(entity.getOperatorHasCapacity(), null));
    form.setLicenseeAcknowledgeOperatorRequirements(Objects.toString(entity.getLicenseeAcknowledgeOperatorRequirements(), null));

    Map<FilePurpose, List<UploadedFileView>> viewMap =
        fileAssociationService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
            new NominationDetailFileReference(entity.getNominationDetail()),
            List.of(NomineeDetailAppendixFileController.PURPOSE.purpose())
        );
    var appendixFileViews = viewMap.getOrDefault(NomineeDetailAppendixFileController.PURPOSE, List.of());
    form.setAppendixDocuments(fileUploadService.getFileUploadFormsFromUploadedFileViews(appendixFileViews));

    return form;
  }
}
