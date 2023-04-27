package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.validation.FrontEndErrorMessage;
import uk.co.nstauthority.offshoresafetydirective.validationutil.DateValidationUtil;

@Service
class NomineeDetailFormValidator implements SmartValidator {

  private static final String NOMINEE_DECLARATIONS_ERROR_MESSAGE = "You must agree to all the licensee declarations";

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

  @Autowired
  NomineeDetailFormValidator(PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
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
          .getOrganisationById(form.getNominatedOrganisationId());

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

    //Need to individually check which checkboxes have not been ticked and assign an error to that specific field
    //This will make sure the error link points to the right unchecked checkbox
    //We also only want to a single error message even if multiple checkboxes are not ticked
    if (form.getOperatorHasAuthority() == null) {
      errors.rejectValue(
          "operatorHasAuthority",
          "operatorHasAuthority.required",
          NOMINEE_DECLARATIONS_ERROR_MESSAGE
      );
    } else if (form.getLicenseeAcknowledgeOperatorRequirements() == null) {
      errors.rejectValue(
          "licenseeAcknowledgeOperatorRequirements",
          "licenseeAcknowledgeOperatorRequirements.required",
          NOMINEE_DECLARATIONS_ERROR_MESSAGE
      );
    } else if (form.getOperatorHasCapacity() == null) {
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
