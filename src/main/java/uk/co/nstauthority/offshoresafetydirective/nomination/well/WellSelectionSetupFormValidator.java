package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.nstauthority.offshoresafetydirective.validationutil.EnumValidationUtil;

@Service
class WellSelectionSetupFormValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return WellSelectionSetupForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, (Object) null);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var t = validationHints.length;
    var form = (WellSelectionSetupForm) target;
    if (form.getWellSelectionType() == null
        || !EnumValidationUtil.isValidEnumValue(WellSelectionType.class, form.getWellSelectionType())) {
      errors.rejectValue(
          "wellSelectionType",
          "wellSelectionType.required",
          "Select if this nomination is in relation to well operatorship"
      );
    }
  }
}
