package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.validation.FrontEndErrorMessage;
import uk.co.nstauthority.offshoresafetydirective.validationutil.DateValidationUtil;
import uk.co.nstauthority.offshoresafetydirective.validationutil.FileValidationUtil;

@Service
class NomineeDetailFormValidator implements SmartValidator {

  private static final String NOMINEE_DECLARATIONS_ERROR_MESSAGE = "You must agree to all the licensee declarations";
  private static final String NO_APPENDIX_C_DOCUMENT_ERROR_MESSAGE = "Upload the Appendix C and any associated documents";

  static final RequestPurpose NOMINATED_ORGANISATION_VALIDATION_PURPOSE =
      new RequestPurpose("Validate that the nominated organisation selected exists in portal");

  static final String NOMINEE_FIELD_NAME = "nominatedOrganisationId";

  static final FrontEndErrorMessage NOMINEE_REQUIRED_ERROR = new FrontEndErrorMessage(
      NOMINEE_FIELD_NAME,
      "%s.required".formatted(NOMINEE_FIELD_NAME),
      "Select the proposed well or installation operator"
  );

  static final FrontEndErrorMessage NOMINEE_NOT_FOUND_IN_PORTAL_ERROR = new FrontEndErrorMessage(
      NOMINEE_FIELD_NAME,
      "%s.notFound".formatted(NOMINEE_FIELD_NAME),
      NOMINEE_REQUIRED_ERROR.message()
  );

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final FileUploadProperties fileUploadProperties;

  @Autowired
  NomineeDetailFormValidator(PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                             FileUploadProperties fileUploadProperties) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.fileUploadProperties = fileUploadProperties;
  }

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return NomineeDetailForm.class.equals(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {

    var form = (NomineeDetailForm) target;

    if (noNomineeProvided(form)) {

      rejectValue(errors, NOMINEE_REQUIRED_ERROR);

    } else {

      var energyPortalOrganisation = portalOrganisationUnitQueryService
          .getOrganisationById(form.getNominatedOrganisationId(), NOMINATED_ORGANISATION_VALIDATION_PURPOSE);

      if (energyPortalOrganisation.isEmpty()) {

        rejectValue(errors, NOMINEE_NOT_FOUND_IN_PORTAL_ERROR);

      } else if (cannotSelectOrganisation(energyPortalOrganisation.get())) {

        rejectValue(errors, new FrontEndErrorMessage(
            NOMINEE_FIELD_NAME,
            "%s.notValid".formatted(NOMINEE_FIELD_NAME),
            "%s is not a valid operator selection".formatted(energyPortalOrganisation.get().name())
        ));
      }
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "reasonForNomination",
        "reasonForNomination.required",
        "Enter why you want to appoint this operator"
    );

    DateValidationUtil.validateDateIsInTheFuture(
        "plannedStart",
        "date the appointment is planned to take effect",
        form.getPlannedStartDay(),
        form.getPlannedStartMonth(),
        form.getPlannedStartYear(),
        errors
    );

    var allowedFileExtensions = FileDocumentType.APPENDIX_C.getAllowedExtensions()
        .orElse(fileUploadProperties.defaultPermittedFileExtensions());

    FileValidationUtil.validator()
        .withMinimumNumberOfFiles(1, NO_APPENDIX_C_DOCUMENT_ERROR_MESSAGE)
        .validate(errors, form.getAppendixDocuments(), "appendixDocuments", allowedFileExtensions);

    //Need to individually check which checkboxes have not been ticked and assign an error to that specific field
    //This will make sure the error link points to the right unchecked checkbox
    //We also only want to a single error message even if multiple checkboxes are not ticked
    if (BooleanUtils.toBooleanObject(form.getOperatorHasAuthority()) == null) {
      errors.rejectValue(
          "operatorHasAuthority",
          "operatorHasAuthority.required",
          NOMINEE_DECLARATIONS_ERROR_MESSAGE
      );
    } else if (BooleanUtils.toBooleanObject(form.getLicenseeAcknowledgeOperatorRequirements()) == null) {
      errors.rejectValue(
          "licenseeAcknowledgeOperatorRequirements",
          "licenseeAcknowledgeOperatorRequirements.required",
          NOMINEE_DECLARATIONS_ERROR_MESSAGE
      );
    } else if (BooleanUtils.toBooleanObject(form.getOperatorHasCapacity()) == null) {
      errors.rejectValue(
          "operatorHasCapacity",
          "operatorHasCapacity.required",
          NOMINEE_DECLARATIONS_ERROR_MESSAGE
      );
    }
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors, @NonNull Object... validationHints) {
    validate(target, errors);
  }

  private boolean noNomineeProvided(NomineeDetailForm form) {
    return form.getNominatedOrganisationId() == null;
  }

  private boolean cannotSelectOrganisation(PortalOrganisationDto portalOrganisationDto) {
    return !portalOrganisationDto.isActive();
  }

  private void rejectValue(Errors errors, FrontEndErrorMessage frontEndErrorMessage) {
    errors.rejectValue(
        frontEndErrorMessage.field(),
        frontEndErrorMessage.code(),
        frontEndErrorMessage.message()
    );
  }
}
