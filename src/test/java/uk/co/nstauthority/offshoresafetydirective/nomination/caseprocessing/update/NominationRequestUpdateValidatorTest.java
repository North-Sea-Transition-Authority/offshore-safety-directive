package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

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
class NominationRequestUpdateValidatorTest {

  @InjectMocks
  private NominationRequestUpdateValidator nominationRequestUpdateValidator;

  @Test
  void supports_assertTrue() {
    assertTrue(nominationRequestUpdateValidator.supports(NominationRequestUpdateForm.class));
  }

  @Test
  void supports_assertFalse() {
    assertFalse(nominationRequestUpdateValidator.supports(UnsupportedClass.class));
  }

  @Test
  void validate_fullForm() {
    var form = new NominationRequestUpdateForm();
    form.getReason().setInputValue("reason");
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    nominationRequestUpdateValidator.validate(form, bindingResult);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_emptyForm() {
    var form = new NominationRequestUpdateForm();
    form.getReason().setInputValue("");
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    nominationRequestUpdateValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "reason.inputValue",
                "reason.required",
                "Enter the information that needs to be updated"
            )
        );
  }

  private static class UnsupportedClass {
  }
}