package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

@ExtendWith(MockitoExtension.class)
class WithdrawNominationValidatorTest {

  @InjectMocks
  private WithdrawNominationValidator withdrawNominationValidator;

  @Test
  void supports_whenUnsupportedClass_thenDoesNotSupport() {
    assertFalse(withdrawNominationValidator.supports(UnsupportedClass.class));
  }

  @Test
  void supports_whenWithdrawNominationForm_thenDoesSupport() {
    assertTrue(withdrawNominationValidator.supports(WithdrawNominationForm.class));
  }

  @Test
  void validate_whenFullyPopulated_thenVerifyNoErrors() {
    var form = new WithdrawNominationForm();
    form.getReason().setInputValue("reason");

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    withdrawNominationValidator.validate(form, bindingResult);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenEmptyForm_thenVerifyErrors() {
    var form = new WithdrawNominationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    withdrawNominationValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "reason.inputValue",
                "reason.required",
                "Enter reason for withdrawal"
            )
        );
  }

  private static class UnsupportedClass {
  }
}