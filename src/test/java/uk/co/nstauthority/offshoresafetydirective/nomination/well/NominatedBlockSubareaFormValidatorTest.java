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
class NominatedBlockSubareaFormValidatorTest {

  private static NominatedBlockSubareaFormValidator nominatedBlockSubareaFormValidator;

  @BeforeAll
  static void setup() {
    nominatedBlockSubareaFormValidator = new NominatedBlockSubareaFormValidator();
  }

  @Test
  void supports_whenNominatedWellDetailForm_thenTrue() {
    assertTrue(nominatedBlockSubareaFormValidator.supports(NominatedBlockSubareaForm.class));
  }

  @Test
  void supports_whenNonSupportedClass_thenFalse() {
    assertFalse(nominatedBlockSubareaFormValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).isEmpty();
  }

  @Test
  void validate_whenNoSubareasSelected_thenError() {
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(Collections.emptyList())
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("subareasSelect", Set.of("subareasSelect.notEmpty"))
    );
  }

  @Test
  void validate_whenValidForFutureWellsInSubareaNotSelected_thenError() {
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withValidForFutureWellsInSubarea(null)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("validForFutureWellsInSubarea", Set.of("validForFutureWellsInSubarea.required"))
    );
  }

  @Test
  void validate_whenForAllWellPhasesNotSelected_thenError() {
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withForAllWellPhases(null)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("forAllWellPhases", Set.of("forAllWellPhases.required"))
    );
  }

  @Test
  void validate_whenNotForAllWellPhasesNotSelectedAndNoPhaseSelected_thenError() {
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(null)
        .withDevelopmentPhase(null)
        .withDecommissioningPhase(null)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("explorationAndAppraisalPhase", Set.of("explorationAndAppraisalPhase.required"))
    );
  }

  @Test
  void validate_whenValidForFutureWellsAndOnlyDecommissioningPhase_thenError() {
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withForAllWellPhases(true)
        .withExplorationAndAppraisalPhase(null)
        .withDevelopmentPhase(null)
        .withDecommissioningPhase(true)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("validForFutureWellsInSubarea", Set.of("validForFutureWellsInSubarea.invalid"))
    );
  }

  private static class NonSupportedClass {

  }
}