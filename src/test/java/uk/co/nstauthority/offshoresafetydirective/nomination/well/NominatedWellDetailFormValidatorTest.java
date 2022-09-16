package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class NominatedWellDetailFormValidatorTest {

  private static NominatedWellDetailFormValidator nominatedWellDetailFormValidator;

  @BeforeAll
  static void setup() {
    nominatedWellDetailFormValidator = new NominatedWellDetailFormValidator();
  }

  @Test
  void supports_whenNominatedWellDetailForm_thenTrue() {
    assertTrue(nominatedWellDetailFormValidator.supports(NominatedWellDetailForm.class));
  }

  @Test
  void supports_whenNonSupportedClass_thenFalse() {
    assertFalse(nominatedWellDetailFormValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    var form = NominatedWellDetailTestUtil.getValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).isEmpty();
  }

  @Test
  void validate_whenNoWellsSelected_thenError() {
    var form = NominatedWellDetailTestUtil.getValidForm();
    form.setWells(Collections.emptyList());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("wellsSelect", Set.of("wellsSelect.notEmpty"))
    );
  }

  @Test
  void validate_whenForAllWellPhasesNotSelected_thenError() {
    var form = NominatedWellDetailTestUtil.getValidForm();
    form.setForAllWellPhases(null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("forAllWellPhases", Set.of("forAllWellPhases.required"))
    );
  }

  @Test
  void validate_whenNotForAllWellPhasesNotSelectedAndNoPhaseSelected_thenError() {
    var form = NominatedWellDetailTestUtil.getValidForm();
    form.setForAllWellPhases(false);
    form.setExplorationAndAppraisalPhase(null);
    form.setDevelopmentPhase(null);
    form.setDecommissioningPhase(null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("explorationAndAppraisalPhase", Set.of("explorationAndAppraisalPhase.required"))
    );
  }

  private static class NonSupportedClass {

  }
}