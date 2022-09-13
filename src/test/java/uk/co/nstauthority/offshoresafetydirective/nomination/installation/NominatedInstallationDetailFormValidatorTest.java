package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

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
class NominatedInstallationDetailFormValidatorTest {

  private static NominatedInstallationDetailFormValidator nominatedInstallationDetailFormValidator;

  @BeforeAll
  static void setup() {
    nominatedInstallationDetailFormValidator = new NominatedInstallationDetailFormValidator();
  }

  @Test
  void supports_whenNominatedInstallationDetailForm_thenTrue() {
    assertTrue(nominatedInstallationDetailFormValidator.supports(NominatedInstallationDetailForm.class));
  }

  @Test
  void supports_whenNotNominatedInstallationDetailForm_thenFalse() {
    assertFalse(nominatedInstallationDetailFormValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenNoInstallationsSelected_thenError() {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withInstallations(Collections.emptyList())
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("installationsSelect", Set.of("installationsSelect.notEmpty"))
    );
  }

  @Test
  void validate_whenValidFormWithAllInstallationPhasesSelected_thenNoErrors() {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(true)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).isEmpty();
  }

  @Test
  void validate_whenValidFormWithNotAllInstallationPhasesSelected_thenNoErrors() {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(false)
        .withDevelopmentConstructionPhase(true)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).isEmpty();
  }

  @Test
  void validate_whenForAllInstallationPhasesNotSelected_thenError() {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(null)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("forAllInstallationPhases", Set.of("forAllInstallationPhases.required"))
    );
  }

  @Test
  void validate_whenNotForAllInstallationPhasesAndNoPhasesSelected_thenError() {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(false)
        .withDevelopmentDesignPhase(null)
        .withDevelopmentConstructionPhase(null)
        .withDevelopmentInstallationPhase(null)
        .withDevelopmentCommissioningPhase(null)
        .withDevelopmentProductionPhase(null)
        .withDecommissioningPhase(null)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).containsExactly(
        entry("developmentDesignPhase", Set.of("developmentDesignPhase.required"))
    );
  }

  private static class NonSupportedClass {

  }
}