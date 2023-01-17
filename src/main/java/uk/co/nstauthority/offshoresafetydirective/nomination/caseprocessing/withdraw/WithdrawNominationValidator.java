package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw;

import javax.annotation.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.fivium.formlibrary.validator.string.StringMandatoryValidationStep;

@Service
class WithdrawNominationValidator implements SmartValidator {

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors, @Nullable Object... validationHints) {
    if (target == null) {
      throw new IllegalArgumentException("Target must not be null. Expecting type of WithdrawNominationForm");
    }
    var form = (WithdrawNominationForm) target;
    new StringMandatoryValidationStep()
        .validationStep(form.getReason(), errors);
  }

  @Override
  public boolean supports(@Nullable Class<?> clazz) {
    return WithdrawNominationForm.class.equals(clazz);
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors) {
    validate(target, errors, new Object[0]);
  }
}
