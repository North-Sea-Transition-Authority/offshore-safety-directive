package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class NominationQaChecksValidatorTest {

  @InjectMocks
  private NominationQaChecksValidator nominationQaChecksValidator;

  @Test
  void supports_assertTrue() {
    assertTrue(nominationQaChecksValidator.supports(NominationQaChecksForm.class));
  }

  @Test
  void supports_assertFalse() {
    assertFalse(nominationQaChecksValidator.supports(UnsupportedClass.class));
  }

  @Test
  void validate_whenAllFieldsEntered_valid() {
    var form = new NominationQaChecksForm();
    form.getComment().setInputValue("comment");
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    nominationQaChecksValidator.validate(form, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenNoFieldsEntered_valid() {
    var form = new NominationQaChecksForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    nominationQaChecksValidator.validate(form, bindingResult);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .containsExactly(
            entry("comment.inputValue", Set.of("Enter QA comments"))
        );
  }

  private static class UnsupportedClass {
  }
}