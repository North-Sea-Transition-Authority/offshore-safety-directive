package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class WellSelectionSetupFormValidatorTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  private static final WellSelectionSetupFormValidatorHint HINT =
      new WellSelectionSetupFormValidatorHint(NOMINATION_DETAIL);

  @InjectMocks
  private WellSelectionSetupFormValidator wellSelectionSetupFormValidator;

  @Test
  void supports_whenWellSetupForm_thenTrue() {
    assertTrue(wellSelectionSetupFormValidator.supports(WellSelectionSetupForm.class));
  }

  @Test
  void supports_whenNonSupportedForm_thenFalse() {
    assertFalse(wellSelectionSetupFormValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenValidForm_assertNoErrors() {
    var validForm = WellSelectionSetupFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(validForm, "form");

    wellSelectionSetupFormValidator.validate(validForm, bindingResult, HINT);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenInvalidForm_assertErrors() {
    var invalidForm = new WellSelectionSetupForm();
    var bindingResult = new BeanPropertyBindingResult(invalidForm, "form");

    wellSelectionSetupFormValidator.validate(invalidForm, bindingResult, HINT);

    assertTrue(bindingResult.hasErrors());

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("wellSelectionType", Set.of("wellSelectionType.required"))
    );
  }

  @Test
  void validate_whenInvalidWellSelectionTypeAnswer_assertErrors() {
    var invalidForm = new WellSelectionSetupForm();
    invalidForm.setWellSelectionType("Invalid well selection type");
    var bindingResult = new BeanPropertyBindingResult(invalidForm, "form");

    wellSelectionSetupFormValidator.validate(invalidForm, bindingResult, HINT);

    assertTrue(bindingResult.hasErrors());

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("wellSelectionType", Set.of("wellSelectionType.required"))
    );
  }

  @Test
  void validate_whenNoHintProvided_expectException() {
    var form = new WellSelectionSetupForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    assertThrows(IllegalStateException.class, () -> wellSelectionSetupFormValidator.validate(form, bindingResult));
  }

  private static class NonSupportedClass {

  }
}