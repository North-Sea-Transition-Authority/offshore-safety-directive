package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.offshoresafetydirective.validationutil.DateValidationUtil;

@Service
class NomineeDetailFormValidator implements SmartValidator {

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

    if (form.getOperatorHasAuthority() == null
        || form.getLicenseeAcknowledgeOperatorRequirements() == null
        || form.getOperatorHasCapacity() == null) {
      errors.rejectValue(
          "operatorHasAuthority",
          "operatorHasAuthority.required",
          "You must agree to all the nominee declarations"
      );
    }
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }
}
