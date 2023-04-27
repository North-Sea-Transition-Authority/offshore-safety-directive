package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
class NominatedBlockSubareaFormValidator implements Validator {

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return NominatedBlockSubareaForm.class.equals(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
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
          "Select Yes if this nomination should cover future wells that may be drilled in the selected subareas"
      );
    }
    if (form.getForAllWellPhases() == null) {
      errors.rejectValue(
          "forAllWellPhases",
          "forAllWellPhases.required",
          "Select Yes if this nomination is for all well activity phases"
      );
    } else if (BooleanUtils.isFalse(form.getForAllWellPhases()) && !anyNominationPhaseSelected(form)) {
      errors.rejectValue(
          "explorationAndAppraisalPhase",
          "explorationAndAppraisalPhase.required",
          "Select which well activity phases this nomination is for"
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
