package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

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

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .containsExactly(
            entry("reason.inputValue", Set.of("Enter reason for withdrawal"))
        );
  }

  private static class UnsupportedClass {
  }
}