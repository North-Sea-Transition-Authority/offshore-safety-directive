package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;

@Service
class ApplicantDetailFormValidator implements SmartValidator {

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return ApplicantDetailForm.class.equals(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors, @NonNull Object... validationHints) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "portalOrganisationId",
        "portalOrganisationId.required",
        "Select what organisation is making this nomination"
    );
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
    validate(target, errors, new Object[0]);
  }
}
