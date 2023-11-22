package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NomineeDetailFormService {

  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private final NomineeDetailFormValidator nomineeDetailFormValidator;
  private final FileService fileService;

  @Autowired
  NomineeDetailFormService(NomineeDetailPersistenceService nomineeDetailPersistenceService,
                           NomineeDetailFormValidator nomineeDetailFormValidator,
                           FileService fileService) {
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.nomineeDetailFormValidator = nomineeDetailFormValidator;
    this.fileService = fileService;
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

    var appendixDocuments = fileService.findAll(
        entity.getNominationDetail().getId().toString(),
        FileUsageType.NOMINATION_DETAIL.getUsageType(),
        FileDocumentType.APPENDIX_C.getDocumentType()
    );

    var appendixDocumentForms = appendixDocuments.stream()
        .map(FileUploadLibraryUtils::asForm)
        .toList();

    form.setAppendixDocuments(appendixDocumentForms);

    return form;
  }
}
