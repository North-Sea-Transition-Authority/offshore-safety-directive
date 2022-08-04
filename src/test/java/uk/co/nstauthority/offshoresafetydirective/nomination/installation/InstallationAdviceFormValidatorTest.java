package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class InstallationAdviceFormValidatorTest {

  private static InstallationAdviceFormValidator installationAdviceFormValidator;

  @BeforeAll
  static void setup() {
    installationAdviceFormValidator = new InstallationAdviceFormValidator();
  }

  @Test
  void supports_whenInstallationAdviceForm_thenTrue() {
    assertTrue(installationAdviceFormValidator.supports(InstallationAdviceForm.class));
  }

  @Test
  void supports_whenNotInstallationAdviceForm_thenFalse() {
    assertFalse(installationAdviceFormValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    var validForm = new InstallationAdviceFormTestUtil.InstallationAdviceFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(validForm, "form");

    installationAdviceFormValidator.validate(validForm, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenInvalidForm_thenAssertErrors() {
    var invalidForm = new InstallationAdviceForm();
    var bindingResult = new BeanPropertyBindingResult(invalidForm, "form");

    installationAdviceFormValidator.validate(invalidForm, bindingResult);

    assertTrue(bindingResult.hasErrors());

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("includeInstallationsInNomination", Set.of("includeInstallationsInNomination.required"))
    );
  }

  private static class NonSupportedClass {

  }
}