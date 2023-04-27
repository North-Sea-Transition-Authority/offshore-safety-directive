package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
class NominatedWellDetailFormValidator implements Validator {

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return NominatedWellDetailForm.class.equals(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
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
          "Select Yes if this nomination is for all well activity phases"
      );
    } else if (BooleanUtils.isFalse(form.getForAllWellPhases()) && !anyNominationPhaseSelected(form)) {
      errors.rejectValue(
          "explorationAndAppraisalPhase",
          "explorationAndAppraisalPhase.required",
          "Select which well activity phases this nomination is for"
      );
    }
  }

  private boolean anyNominationPhaseSelected(NominatedWellDetailForm form) {
    return !(form.getExplorationAndAppraisalPhase() == null
        && form.getDevelopmentPhase() == null
        && form.getDecommissioningPhase() == null);
  }
}
