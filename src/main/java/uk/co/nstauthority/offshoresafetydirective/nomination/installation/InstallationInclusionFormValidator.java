package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationAssetType;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationTypeValidator;

@Service
class InstallationInclusionFormValidator implements SmartValidator {

  private static final String INCLUDE_INSTALLATIONS_FIELD_NAME = "includeInstallationsInNomination";

  private final NominationTypeValidator nominationTypeValidator;

  @Autowired
  InstallationInclusionFormValidator(NominationTypeValidator nominationTypeValidator) {
    this.nominationTypeValidator = nominationTypeValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return InstallationInclusionForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    InstallationInclusionFormValidatorHint hint = Arrays.stream(validationHints)
        .filter(Objects::nonNull)
        .filter(validationHint -> validationHint.getClass().equals(InstallationInclusionFormValidatorHint.class))
        .map(InstallationInclusionFormValidatorHint.class::cast)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Cannot get InstallationInclusionFormValidatorHint"));

    var form = (InstallationInclusionForm) target;
    if (form.getIncludeInstallationsInNomination() == null) {
      errors.rejectValue(
          INCLUDE_INSTALLATIONS_FIELD_NAME,
          "%s.required".formatted(INCLUDE_INSTALLATIONS_FIELD_NAME),
          "Select if this nomination is in relation to installation operatorship"
      );
    }

    if (BooleanUtils.isFalse(form.getIncludeInstallationsInNomination())) {
      nominationTypeValidator.validateNominationExclusionAssetTypes(
          errors,
          hint.nominationDetail(),
          NominationAssetType.INSTALLATION,
          INCLUDE_INSTALLATIONS_FIELD_NAME
      );
    }
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, (Object) null);
  }
}
