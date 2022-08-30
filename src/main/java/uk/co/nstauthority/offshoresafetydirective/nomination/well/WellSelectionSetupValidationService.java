package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class WellSelectionSetupValidationService {

  private final WellSelectionSetupFormValidator wellSelectionSetupFormValidator;

  @Autowired
  WellSelectionSetupValidationService(WellSelectionSetupFormValidator wellSelectionSetupFormValidator) {
    this.wellSelectionSetupFormValidator = wellSelectionSetupFormValidator;
  }

  BindingResult validate(WellSelectionSetupForm form, BindingResult bindingResult, NominationDetail nominationDetail) {
    wellSelectionSetupFormValidator.validate(
        form,
        bindingResult,
        new WellSelectionSetupFormValidatorHint(nominationDetail)
    );
    return bindingResult;
  }
}
