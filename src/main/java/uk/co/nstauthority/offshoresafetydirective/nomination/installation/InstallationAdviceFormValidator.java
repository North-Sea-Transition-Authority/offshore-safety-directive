package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

@Service
class InstallationAdviceFormValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return InstallationAdviceForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (InstallationAdviceForm) target;
    if (form.getIncludeInstallationsInNomination() == null) {
      errors.rejectValue(
          "includeInstallationsInNomination",
          "includeInstallationsInNomination.required",
          "Select if this nomination is in relation to installation operatorship"
      );
    }
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, (Object) null);
  }
}
