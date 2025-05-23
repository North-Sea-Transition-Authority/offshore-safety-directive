package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationTypeValidator;

@ExtendWith(MockitoExtension.class)
class InstallationInclusionFormValidatorTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  private static final InstallationInclusionFormValidatorHint HINT =
      new InstallationInclusionFormValidatorHint(NOMINATION_DETAIL);

  @Mock
  protected NominationTypeValidator nominationTypeValidator;

  @InjectMocks
  private InstallationInclusionFormValidator installationInclusionFormValidator;

  @Test
  void supports_whenInstallationAdviceForm_thenTrue() {
    assertTrue(installationInclusionFormValidator.supports(InstallationInclusionForm.class));
  }

  @Test
  void supports_whenNotInstallationAdviceForm_thenFalse() {
    assertFalse(installationInclusionFormValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    var validForm = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(validForm, "form");

    installationInclusionFormValidator.validate(validForm, bindingResult, HINT);

    assertFalse(bindingResult.hasErrors());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenInvalidForm_thenAssertErrors(String invalidValue) {
    var invalidForm = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder()
        .includeInstallationsInNomination(invalidValue)
        .build();
    var bindingResult = new BeanPropertyBindingResult(invalidForm, "form");

    installationInclusionFormValidator.validate(invalidForm, bindingResult, HINT);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "includeInstallationsInNomination",
                "includeInstallationsInNomination.required",
                "Select if this nomination is in relation to installation operatorship"
            )
        );
  }

  @Test
  void validate_whenNoHintProvided_expectException() {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    assertThrows(IllegalStateException.class, () -> installationInclusionFormValidator.validate(form, bindingResult));
  }

  private static class NonSupportedClass {

  }
}