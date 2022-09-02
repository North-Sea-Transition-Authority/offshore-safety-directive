package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

@Service
class NominatedInstallationDetailFormValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return NominatedInstallationDetailForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (NominatedInstallationDetailForm) target;
    if (form.getInstallations() == null || form.getInstallations().isEmpty()) {
      errors.rejectValue(
          "installationsSelect",
          "installationsSelect.notEmpty",
          "You must select at least one installation"
      );
    }
    if (form.getForAllInstallationPhases() == null) {
      errors.rejectValue(
          "forAllInstallationPhases",
          "forAllInstallationPhases.required",
          "Select if this nomination is for all installation phases"
      );
    } else if (BooleanUtils.isFalse(form.getForAllInstallationPhases()) && !anyNominationPhaseSelected(form)) {
      errors.rejectValue(
          "developmentDesignPhase",
          "developmentDesignPhase.required",
          "Select which installation phases this nomination is for"
      );
    }
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, (Object) null);
  }

  private boolean anyNominationPhaseSelected(NominatedInstallationDetailForm form) {
    return !(form.getDevelopmentDesignPhase() == null
        && form.getDevelopmentConstructionPhase() == null
        && form.getDevelopmentInstallationPhase() == null
        && form.getDevelopmentCommissioningPhase() == null
        && form.getDevelopmentProductionPhase() == null
        && form.getDecommissioningPhase() == null);
  }
}
