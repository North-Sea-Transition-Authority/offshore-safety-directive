package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailFormValidatorTest {

  private ApplicantDetailFormValidator applicantDetailFormValidator;

  @BeforeEach
  void setup() {
    applicantDetailFormValidator = new ApplicantDetailFormValidator();
  }

  @Test
  void supports_whenApplicationDetailFormClass_thenTrue() {
    var supportedClass = ApplicantDetailForm.class;

    assertTrue(applicantDetailFormValidator.supports(supportedClass));
  }

  @Test
  void supports_whenNotApplicationDetailFormClass_thenFalse() {
    var nonSupportedClass = NonSupportedClass.class;

    assertFalse(applicantDetailFormValidator.supports(nonSupportedClass));
  }

  @Test
  void validate_whenValidForm_thenNoValidationErrors() {
    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId(1);
    form.setApplicantReference("ref#1");
    var bindingResult = validateApplicantDetailsForm(form);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenInvalidForm_thenValidationErrors() {
    var form = new ApplicantDetailForm();
    var bindingResult = validateApplicantDetailsForm(form);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("portalOrganisationId", Set.of("portalOrganisationId.required"))
    );
  }

  private BindingResult validateApplicantDetailsForm(ApplicantDetailForm form) {
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    applicantDetailFormValidator.validate(form, bindingResult);

    return bindingResult;
  }

  private static class NonSupportedClass {
  }
}