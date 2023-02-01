package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

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
class GeneralCaseNoteValidatorTest {

  @InjectMocks
  private GeneralCaseNoteValidator generalCaseNoteValidator;

  @Test
  void validate_whenFormIsValid_thenVerifyNoErrors() {
    var form = new GeneralCaseNoteForm();
    form.getCaseNoteSubject().setInputValue("Subject");
    form.getCaseNoteText().setInputValue("Case note body text");
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    generalCaseNoteValidator.validate(form, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenAllFieldsEmpty_thenVerifyErrors() {
    var form = new GeneralCaseNoteForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    generalCaseNoteValidator.validate(form, bindingResult);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .containsExactly(
            entry("caseNoteSubject.inputValue", Set.of("Enter a case note subject")),
            entry("caseNoteText.inputValue", Set.of("Enter case note text"))
        );
  }

  @Test
  void supports_unsupportedClass() {
    assertFalse(generalCaseNoteValidator.supports(UnsupportedClass.class));
  }

  @Test
  void supports_supportedClass() {
    assertTrue(generalCaseNoteValidator.supports(GeneralCaseNoteForm.class));
  }

  private static class UnsupportedClass {
  }
}