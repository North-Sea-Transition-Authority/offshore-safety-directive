package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailFormValidator.WELL_QUERY_REQUEST_PURPOSE;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;

@ExtendWith(MockitoExtension.class)
class NominatedWellDetailFormValidatorTest {

  private static NominatedWellDetailFormValidator nominatedWellDetailFormValidator;

  @Mock
  private WellQueryService wellQueryService;

  @BeforeEach
  void setUp() {
    nominatedWellDetailFormValidator = new NominatedWellDetailFormValidator(wellQueryService);
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
    var wellboreId = new WellboreId(10);
    var form = NominatedWellFormTestUtil.builder()
        .withWell(wellboreId.id())
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var wellDto = WellDtoTestUtil.builder()
        .withWellboreId(wellboreId.id())
        .build();
    when(wellQueryService.getWellsByIds(Set.of(wellboreId), WELL_QUERY_REQUEST_PURPOSE))
        .thenReturn(List.of(wellDto));

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_whenNoWellsSelected_thenError() {
    var form = NominatedWellFormTestUtil.builder().build();
    form.setWells(Collections.emptyList());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "wellsSelect",
                "wellsSelect.notEmpty",
                "You must select at least one well"
            )
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenForAllWellPhasesInvalidValue_thenError(String value) {
    var wellboreId = new WellboreId(10);
    var form = NominatedWellFormTestUtil.builder()
        .isForAllWellPhases(value)
        .withWell(wellboreId.id())
        .build();

    var wellDto = WellDtoTestUtil.builder()
        .withWellboreId(wellboreId.id())
        .build();
    when(wellQueryService.getWellsByIds(Set.of(wellboreId), WELL_QUERY_REQUEST_PURPOSE))
        .thenReturn(List.of(wellDto));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "forAllWellPhases",
                "forAllWellPhases.required",
                "Select Yes if this nomination is for all well activity phases"
            )
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenNotForAllWellPhasesNotSelectedAndNoPhaseSelected_thenError(String value) {
    var wellboreId = new WellboreId(10);
    var form = NominatedWellFormTestUtil.builder()
        .withWell(wellboreId.id())
        .isForAllWellPhases(false)
        .isExplorationAndAppraisalPhase(value)
        .isDevelopmentPhase(value)
        .isDecommissioningPhase(value)
        .build();

    var wellDto = WellDtoTestUtil.builder()
        .withWellboreId(wellboreId.id())
        .build();
    when(wellQueryService.getWellsByIds(Set.of(wellboreId), WELL_QUERY_REQUEST_PURPOSE))
        .thenReturn(List.of(wellDto));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "explorationAndAppraisalPhase",
                "explorationAndAppraisalPhase.required",
                "Select which well activity phases this nomination is for"
            )
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

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "wellsSelect",
                "wellsSelect.notEmpty",
                "You must select at least one well"
            )
        );
  }

  @Test
  void validate_whenInvalidWellsSelected_emptyList_thenHasError() {
    var form = NominatedWellFormTestUtil.builder()
        .withWells(List.of())
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "wellsSelect",
                "wellsSelect.notEmpty",
                "You must select at least one well"
            )
        );
  }

  @Test
  void validate_whenInvalidWellsSelected_nonNumericTypeOnly_thenHasError() {
    var form = NominatedWellFormTestUtil.builder()
        .withWells(List.of("fish"))
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "wellsSelect",
                "wellsSelect.notEmpty",
                "You must select at least one well"
            )
        );
  }

  @Test
  void validate_whenInvalidWellsSelected_andMultipleValues_thenHasError() {
    var form = NominatedWellFormTestUtil.builder()
        .withWells(List.of("123", "fish"))
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "wellsSelect",
                "wellsSelect.notAllSelectable",
                "You can only submit valid wells"
            )
        );
  }

  @Test
  void validate_whenWellIsNotOnPortal_thenHasError() {
    var wellboreId = new WellboreId(10);
    var form = NominatedWellFormTestUtil.builder()
        .withWell(wellboreId.id())
        .build();

    when(wellQueryService.getWellsByIds(Set.of(wellboreId), WELL_QUERY_REQUEST_PURPOSE))
        .thenReturn(List.of());

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "wellsSelect",
                "wellsSelect.notAllSelectable",
                "You can only submit valid wells"
            )
        );
  }

  @Test
  void validate_whenNotForAllWellPhases_andAllPhasesSelected_thenError() {
    var wellId = 10;
    var form = NominatedWellFormTestUtil.builder()
        .isForAllWellPhases(false)
        .isExplorationAndAppraisalPhase(true)
        .isDevelopmentPhase(true)
        .isDecommissioningPhase(true)
        .withWell(wellId)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var wellDto = WellDtoTestUtil.builder()
        .withWellboreId(wellId)
        .build();
    when(wellQueryService.getWellsByIds(Set.of(new WellboreId(wellId)), WELL_QUERY_REQUEST_PURPOSE))
        .thenReturn(List.of(wellDto));

    nominatedWellDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "forAllWellPhases",
                "forAllWellPhases.selectedAll",
                "Select Yes if all phases are applicable"
            )
        );
  }

  private static class NonSupportedClass {

  }
}