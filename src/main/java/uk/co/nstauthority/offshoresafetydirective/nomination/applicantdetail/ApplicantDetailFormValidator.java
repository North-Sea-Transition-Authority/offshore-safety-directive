package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;

@Service
class ApplicantDetailFormValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ApplicantDetailForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "portalOrganisationId",
        "portalOrganisationId.required",
        "Select what organisation is making this nomination"
    );
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }
}
