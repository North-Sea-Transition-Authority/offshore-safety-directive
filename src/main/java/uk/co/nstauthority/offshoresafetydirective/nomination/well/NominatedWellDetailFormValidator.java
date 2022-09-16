package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

@Service
class NominatedWellDetailFormValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return NominatedWellDetailForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, (Object) null);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (NominatedWellDetailForm) target;
    if (form.getWells() == null || form.getWells().isEmpty()) {
      errors.rejectValue(
          "wellsSelect",
          "wellsSelect.notEmpty",
          "You must select at least one well"
      );
    }

    if (form.getForAllWellPhases() == null) {
      errors.rejectValue(
          "forAllWellPhases",
          "forAllWellPhases.required",
          "Select if this nomination is for all well phases"
      );
    } else if (BooleanUtils.isFalse(form.getForAllWellPhases()) && !anyNominationPhaseSelected(form)) {
      errors.rejectValue(
          "explorationAndAppraisalPhase",
          "explorationAndAppraisalPhase.required",
          "Select which well phases this nomination is for"
      );
    }
  }

  private boolean anyNominationPhaseSelected(NominatedWellDetailForm form) {
    return !(form.getExplorationAndAppraisalPhase() == null
        && form.getDevelopmentPhase() == null
        && form.getDecommissioningPhase() == null);
  }
}
