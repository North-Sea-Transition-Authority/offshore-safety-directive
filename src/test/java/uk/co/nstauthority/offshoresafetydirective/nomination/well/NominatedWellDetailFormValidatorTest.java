package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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
    var form = NominatedWellFormTestUtil.builder()
        .withWell(10)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).isEmpty();
  }

  @Test
  void validate_whenNoWellsSelected_thenError() {
    var form = NominatedWellFormTestUtil.builder().build();
    form.setWells(Collections.emptyList());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("wellsSelect", Set.of("wellsSelect.notEmpty"))
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenForAllWellPhasesInvalidValue_thenError(String value) {
    var form = NominatedWellFormTestUtil.builder()
        .isForAllWellPhases(value)
        .withWell(10)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("forAllWellPhases", Set.of("forAllWellPhases.required"))
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenNotForAllWellPhasesNotSelectedAndNoPhaseSelected_thenError(String value) {
    var form = NominatedWellFormTestUtil.builder()
        .withWell(10)
        .isForAllWellPhases(false)
        .isExplorationAndAppraisalPhase(value)
        .isDevelopmentPhase(value)
        .isDecommissioningPhase(value)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("explorationAndAppraisalPhase", Set.of("explorationAndAppraisalPhase.required"))
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenInvalidWellsSelected_nullAndEmptySource_thenHasError(List<String> invalidValue) {
    var form = NominatedWellFormTestUtil.builder()
        .withWells(invalidValue)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("wellsSelect", Set.of("wellsSelect.notEmpty"))
    );
  }

  @Test
  void validate_whenInvalidWellsSelected_emptyList_thenHasError() {
    var form = NominatedWellFormTestUtil.builder()
        .withWells(List.of())
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("wellsSelect", Set.of("wellsSelect.notEmpty"))
    );
  }

  @Test
  void validate_whenInvalidWellsSelected_nonNumericTypeOnly_thenHasError() {
    var form = NominatedWellFormTestUtil.builder()
        .withWells(List.of("fish"))
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("wellsSelect", Set.of("wellsSelect.notEmpty"))
    );
  }

  @Test
  void validate_whenInvalidWellsSelected_andMultipleValues_thenHasError() {
    var form = NominatedWellFormTestUtil.builder()
        .withWells(List.of("123", "fish"))
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).isEmpty();
  }

  private static class NonSupportedClass {

  }
}