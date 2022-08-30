package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Arrays;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationAssetType;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationTypeValidator;
import uk.co.nstauthority.offshoresafetydirective.validationutil.EnumValidationUtil;

@Service
class WellSelectionSetupFormValidator implements SmartValidator {

  private static final String WELL_SELECTION_TYPE_FIELD_NAME = "wellSelectionType";

  private final NominationTypeValidator nominationTypeValidator;

  @Autowired
  WellSelectionSetupFormValidator(NominationTypeValidator nominationTypeValidator) {
    this.nominationTypeValidator = nominationTypeValidator;
  }

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
    WellSelectionSetupFormValidatorHint hint = Arrays.stream(validationHints)
        .filter(Objects::nonNull)
        .filter(validationHint -> validationHint.getClass().equals(WellSelectionSetupFormValidatorHint.class))
        .map(WellSelectionSetupFormValidatorHint.class::cast)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Cannot get WellSelectionSetupFormValidatorHint"));

    var form = (WellSelectionSetupForm) target;
    if (form.getWellSelectionType() == null
        || !EnumValidationUtil.isValidEnumValue(WellSelectionType.class, form.getWellSelectionType())) {
      errors.rejectValue(
          WELL_SELECTION_TYPE_FIELD_NAME,
          "%s.required".formatted(WELL_SELECTION_TYPE_FIELD_NAME),
          "Select if this nomination is in relation to well operatorship"
      );
    }

    if (form.getWellSelectionType() != null
        && EnumValidationUtil.isValidEnumValue(WellSelectionType.class, form.getWellSelectionType())
        && WellSelectionType.valueOf(form.getWellSelectionType()).equals(WellSelectionType.NO_WELLS)) {
      nominationTypeValidator.validateNominationExclusionAssetTypes(
          errors,
          hint.nominationDetail(),
          NominationAssetType.WELL,
          WELL_SELECTION_TYPE_FIELD_NAME
      );
    }
  }
}
