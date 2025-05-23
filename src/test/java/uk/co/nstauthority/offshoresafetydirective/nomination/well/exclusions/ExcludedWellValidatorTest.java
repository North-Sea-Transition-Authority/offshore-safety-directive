package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaWellboreService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryItemView;

@ExtendWith(MockitoExtension.class)
class ExcludedWellValidatorTest {

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();

  @Mock
  private NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  @Mock
  private LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService;

  @InjectMocks
  private ExcludedWellValidator excludedWellValidator;

  @Test
  void supports_whenSupported_thenTrue() {
    var supportedClass = excludedWellValidator.supports(WellExclusionForm.class);
    assertThat(supportedClass).isTrue();
  }

  @Test
  void supports_whenNotSupported_thenFalse() {
    var supportedClass = excludedWellValidator.supports(UnsupportedClass.class);
    assertThat(supportedClass).isFalse();
  }

  @Test
  void validate_whenNoHintVariant_thenUnsupportedException() {

    var form = new WellExclusionForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    assertThatThrownBy(() -> excludedWellValidator.validate(form, bindingResult))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenInvalidHasWellsToExcludeForm_thenVerifyErrors(String invalidValue) {

    var form = WellExclusionFormTestUtil.builder()
        .hasWellsToExclude(invalidValue)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    excludedWellValidator.validate(form, bindingResult, new ExcludedWellValidatorHint(NOMINATION_DETAIL));

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                ExcludedWellValidator.HAS_WELL_TO_EXCLUDE_REQUIRED.field(),
                ExcludedWellValidator.HAS_WELL_TO_EXCLUDE_REQUIRED.code(),
                ExcludedWellValidator.HAS_WELL_TO_EXCLUDE_REQUIRED.message()
            )
        );
  }

  @Test
  void validate_whenNotExcludingWells_thenNoErrors() {

    var form = WellExclusionFormTestUtil.builder()
        .hasWellsToExclude(false)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    excludedWellValidator.validate(form, bindingResult, new ExcludedWellValidatorHint(NOMINATION_DETAIL));

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_whenExcludingWellsAndNoWellsInSubarea_thenError() {

    var form = WellExclusionFormTestUtil.builder()
        .hasWellsToExclude(true)
        .build();

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(NOMINATION_DETAIL))
        .willReturn(Collections.emptyList());

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    excludedWellValidator.validate(form, bindingResult, new ExcludedWellValidatorHint(NOMINATION_DETAIL));

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                ExcludedWellValidator.NO_WELLS_IN_SUBAREAS.field(),
                ExcludedWellValidator.NO_WELLS_IN_SUBAREAS.code(),
                ExcludedWellValidator.NO_WELLS_IN_SUBAREAS.message()
            )
        );
  }

  @Test
  void validate_whenExcludingWellsAndNoWellsSelected_thenError() {

    var form = WellExclusionFormTestUtil.builder()
        .hasWellsToExclude(true)
        .withExcludedWells(Collections.emptyList())
        .build();

    var expectedSubarea  = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"), "subarea name");

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(NOMINATION_DETAIL))
        .willReturn(List.of(expectedSubarea));

    var wellDto = WellDtoTestUtil.builder().build();
    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(List.of(expectedSubarea.subareaId())))
        .willReturn(List.of(WellSummaryItemView.fromWellDto(wellDto)));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    excludedWellValidator.validate(form, bindingResult, new ExcludedWellValidatorHint(NOMINATION_DETAIL));

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                ExcludedWellValidator.WELL_TO_EXCLUDE_EMPTY.field(),
                ExcludedWellValidator.WELL_TO_EXCLUDE_EMPTY.code(),
                ExcludedWellValidator.WELL_TO_EXCLUDE_EMPTY.message()
            )
        );
  }

  @Test
  void validate_whenExcludingWellsAndWellsSelected_thenNoError() {

    var form = WellExclusionFormTestUtil.builder()
        .hasWellsToExclude(true)
        .withExcludedWell("wellbore id")
        .build();

    var expectedSubarea  = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"), "subarea name");

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(NOMINATION_DETAIL))
        .willReturn(List.of(expectedSubarea));

    var wellDto = WellDtoTestUtil.builder().build();
    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(List.of(expectedSubarea.subareaId())))
        .willReturn(List.of(WellSummaryItemView.fromWellDto(wellDto)));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    excludedWellValidator.validate(form, bindingResult, new ExcludedWellValidatorHint(NOMINATION_DETAIL));

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  static class UnsupportedClass {}
}