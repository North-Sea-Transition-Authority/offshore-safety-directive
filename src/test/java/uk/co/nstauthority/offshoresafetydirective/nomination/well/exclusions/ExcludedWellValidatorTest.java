package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaWellboreService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

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

  @Test
  void validate_whenEmptyForm_thenVerifyErrors() {

    var form = new WellExclusionForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    excludedWellValidator.validate(form, bindingResult, new ExcludedWellValidatorHint(NOMINATION_DETAIL));

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(resultingErrorCodes).containsExactly(
        entry(
            ExcludedWellValidator.HAS_WELL_TO_EXCLUDE_REQUIRED.field(),
            Set.of(ExcludedWellValidator.HAS_WELL_TO_EXCLUDE_REQUIRED.code())
        )
    );

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(resultingErrorMessages).containsExactly(
        entry(
            ExcludedWellValidator.HAS_WELL_TO_EXCLUDE_REQUIRED.field(),
            Set.of(ExcludedWellValidator.HAS_WELL_TO_EXCLUDE_REQUIRED.message())
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

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(resultingErrorCodes).isEmpty();
    assertThat(resultingErrorMessages).isEmpty();
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

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(resultingErrorCodes).containsExactly(
        entry(
            ExcludedWellValidator.NO_WELLS_IN_SUBAREAS.field(),
            Set.of(ExcludedWellValidator.NO_WELLS_IN_SUBAREAS.code())
        )
    );

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(resultingErrorMessages).containsExactly(
        entry(
            ExcludedWellValidator.NO_WELLS_IN_SUBAREAS.field(),
            Set.of(ExcludedWellValidator.NO_WELLS_IN_SUBAREAS.message())
        )
    );
  }

  @Test
  void validate_whenExcludingWellsAndNoWellsSelected_thenError() {

    var form = WellExclusionFormTestUtil.builder()
        .hasWellsToExclude(true)
        .withExcludedWells(Collections.emptyList())
        .build();

    var expectedSubarea  = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(NOMINATION_DETAIL))
        .willReturn(List.of(expectedSubarea));

    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(List.of(expectedSubarea.subareaId())))
        .willReturn(List.of(WellDtoTestUtil.builder().build()));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    excludedWellValidator.validate(form, bindingResult, new ExcludedWellValidatorHint(NOMINATION_DETAIL));

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(resultingErrorCodes).containsExactly(
        entry(
            ExcludedWellValidator.WELL_TO_EXCLUDE_EMPTY.field(),
            Set.of(ExcludedWellValidator.WELL_TO_EXCLUDE_EMPTY.code())
        )
    );

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(resultingErrorMessages).containsExactly(
        entry(
            ExcludedWellValidator.WELL_TO_EXCLUDE_EMPTY.field(),
            Set.of(ExcludedWellValidator.WELL_TO_EXCLUDE_EMPTY.message())
        )
    );
  }

  @Test
  void validate_whenExcludingWellsAndWellsSelected_thenNoError() {

    var form = WellExclusionFormTestUtil.builder()
        .hasWellsToExclude(true)
        .withExcludedWell("wellbore id")
        .build();

    var expectedSubarea  = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(NOMINATION_DETAIL))
        .willReturn(List.of(expectedSubarea));

    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(List.of(expectedSubarea.subareaId())))
        .willReturn(List.of(WellDtoTestUtil.builder().build()));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    excludedWellValidator.validate(form, bindingResult, new ExcludedWellValidatorHint(NOMINATION_DETAIL));

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(resultingErrorCodes).isEmpty();

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(resultingErrorMessages).isEmpty();

  }

  static class UnsupportedClass {}
}