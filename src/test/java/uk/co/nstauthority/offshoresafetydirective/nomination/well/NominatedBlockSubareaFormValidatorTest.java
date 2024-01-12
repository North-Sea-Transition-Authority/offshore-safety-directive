package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaFormValidator.LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class NominatedBlockSubareaFormValidatorTest {

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @InjectMocks
  private NominatedBlockSubareaFormValidator nominatedBlockSubareaFormValidator;

  @BeforeEach
  void setUp() {
    nominatedBlockSubareaFormValidator = new NominatedBlockSubareaFormValidator(
        licenceBlockSubareaQueryService);
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    var subareaIdOnForm = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(subareaIdOnForm.id()))
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var subareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(subareaIdOnForm.id())
        .build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(subareaIdOnForm),
        LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(subareaDto));

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

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenValidForFutureWellsInSubareaNotSelected_thenError(String value) {
    var subareaIdOnForm = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(subareaIdOnForm.id()))
        .withValidForFutureWellsInSubarea(value)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var subareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(subareaIdOnForm.id())
        .build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(subareaIdOnForm),
        LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(subareaDto));

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("validForFutureWellsInSubarea", Set.of("validForFutureWellsInSubarea.required"))
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenForAllWellPhasesNotSelected_thenError(String value) {
    var subareaIdOnForm = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(subareaIdOnForm.id()))
        .withForAllWellPhases(value)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var subareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(subareaIdOnForm.id())
        .build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(subareaIdOnForm),
        LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(subareaDto));

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("forAllWellPhases", Set.of("forAllWellPhases.required"))
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenNotForAllWellPhasesNotSelectedAndNoPhaseSelected_thenError(String value) {
    var subareaIdOnForm = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(subareaIdOnForm.id()))
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(value)
        .withDevelopmentPhase(value)
        .withDecommissioningPhase(value)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var subareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(subareaIdOnForm.id())
        .build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(subareaIdOnForm),
        LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(subareaDto));

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("explorationAndAppraisalPhase", Set.of("explorationAndAppraisalPhase.required"))
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenValidForFutureWellsAndOnlyDecommissioningPhase_thenError(String value) {
    var subareaIdOnForm = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(subareaIdOnForm.id()))
        .withForAllWellPhases(true)
        .withExplorationAndAppraisalPhase(value)
        .withDevelopmentPhase(value)
        .withDecommissioningPhase(true)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var subareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(subareaIdOnForm.id())
        .build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(subareaIdOnForm),
        LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(subareaDto));

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("validForFutureWellsInSubarea", Set.of("validForFutureWellsInSubarea.invalid"))
    );
  }

  @Test
  void validate_whenSubareasSelectedAreNotOnPortal_thenVerifyError() {
    var subareaIdOnForm = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(subareaIdOnForm.id()))
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(subareaIdOnForm),
        LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of());

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("subareasSelect", Set.of("subareasSelect.notEmpty"))
    );
  }

  @Test
  void validate_whenSubareasSelectedAreNotExtant_thenVerifyError() {
    var subareaIdOnForm = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(subareaIdOnForm.id()))
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var licenceBlockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .isExtant(false)
        .build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(subareaIdOnForm),
        LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(licenceBlockSubareaDto));

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("subareasSelect", Set.of("subareasSelect.invalidSubarea"))
    );
  }

  @Test
  void validate_whenValidAndInvalidSubareasSelected_thenVerifyError() {
    var validSubareaIdOnForm = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    var invalidSubareaIdOnForm = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(validSubareaIdOnForm.id(), invalidSubareaIdOnForm.id()))
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var subareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(validSubareaIdOnForm.id())
        .build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(validSubareaIdOnForm, invalidSubareaIdOnForm),
        LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(subareaDto));

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("subareasSelect", Set.of("subareasSelect.invalidSubarea"))
    );
  }

  @Test
  void validate_whenNotForAllWellPhases_andAllPhasesSelected_thenError() {
    var subareaId = "subarea/id";
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(true)
        .withDevelopmentPhase(true)
        .withDecommissioningPhase(true)
        .withSubareas(List.of(subareaId))
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var subareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(subareaId)
        .build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(new LicenceBlockSubareaId(subareaId)),
        LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(subareaDto));

    nominatedBlockSubareaFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("forAllWellPhases", Set.of("forAllWellPhases.selectedAll"))
    );
  }
}