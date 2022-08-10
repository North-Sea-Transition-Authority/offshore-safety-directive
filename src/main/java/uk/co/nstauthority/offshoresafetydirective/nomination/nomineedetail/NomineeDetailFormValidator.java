package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.offshoresafetydirective.validationutil.DateValidationUtil;

@Service
class NomineeDetailFormValidator implements SmartValidator {

  private static final String NOMINEE_DECLARATIONS_ERROR_MESSAGE = "You must agree to all the nominee declarations";

  @Override
  public boolean supports(Class<?> clazz) {
    return NomineeDetailForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (NomineeDetailForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "nominatedOrganisationId",
        "nominatedOrganisationId.required",
        "Select the proposed well or installation operator"
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "reasonForNomination",
        "reasonForNomination.required",
        "Enter the reason for the nomination"
    );

    DateValidationUtil.validateDateIsInTheFuture(
        "plannedStart",
        "planned start date",
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
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }
}
