package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationTypeValidator;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class InstallationInclusionFormValidatorTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  private static final InstallationInclusionFormValidatorHint HINT =
      new InstallationInclusionFormValidatorHint(NOMINATION_DETAIL);

  @Mock
  private NominationTypeValidator nominationTypeValidator;

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

  @Test
  void validate_whenInvalidForm_thenAssertErrors() {
    var invalidForm = new InstallationInclusionForm();
    var bindingResult = new BeanPropertyBindingResult(invalidForm, "form");

    installationInclusionFormValidator.validate(invalidForm, bindingResult, HINT);

    assertTrue(bindingResult.hasErrors());

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("includeInstallationsInNomination", Set.of("includeInstallationsInNomination.required"))
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