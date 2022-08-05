package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

@Service
class NominatedBlockSubareaFormValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return NominatedBlockSubareaForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, (Object) null);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (NominatedBlockSubareaForm) target;
    if (form.getSubareas() == null || form.getSubareas().isEmpty()) {
      errors.rejectValue(
          "subareasSelect",
          "subareasSelect.notEmpty",
          "You must select at least one licence block subarea"
      );
    }
    if (form.getValidForFutureWellsInSubarea() == null) {
      errors.rejectValue(
          "validForFutureWellsInSubarea",
          "validForFutureWellsInSubarea.required",
          "Select if this nomination is for future wells drilled in the selected subareas"
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

    if (BooleanUtils.isTrue(form.getValidForFutureWellsInSubarea())
        && BooleanUtils.isTrue(form.getDecommissioningPhase())
        && form.getExplorationAndAppraisalPhase() == null
        && form.getDevelopmentPhase() == null) {
      errors.rejectValue(
          "validForFutureWellsInSubarea",
          "validForFutureWellsInSubarea.invalid",
          "Cannot set this nomination for all future wells when then only selected well phase is decommissioning"
      );
    }
  }

  private boolean anyNominationPhaseSelected(NominatedBlockSubareaForm form) {
    return !(form.getExplorationAndAppraisalPhase() == null
        && form.getDevelopmentPhase() == null
        && form.getDecommissioningPhase() == null);
  }
}
