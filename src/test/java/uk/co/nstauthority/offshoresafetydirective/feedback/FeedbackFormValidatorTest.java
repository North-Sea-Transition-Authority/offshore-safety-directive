package uk.co.nstauthority.offshoresafetydirective.feedback;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class FeedbackFormValidatorTest {

  private static FeedbackFormValidator feedbackFormValidator;

  @BeforeAll
  static void setUp() {
    feedbackFormValidator = new FeedbackFormValidator();
  }

  @Test
  void supports_whenFeedbackFormClass_thenTrue() {
    var supportedClass = FeedbackForm.class;

    assertTrue(feedbackFormValidator.supports(supportedClass));
  }

  @Test
  void supports_whenNotFeedbackFormClass_thenFalse() {
    var supportedClass = NonSupportedClass.class;

    assertFalse(feedbackFormValidator.supports(supportedClass));
  }

  @Test
  void validate_whenValidForm_thenNoValidationErrors() {
    var form = FeedbackFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    feedbackFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_whenEmptyForm_then() {
    var form = new FeedbackForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    feedbackFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    var extractedErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(extractedErrors)
        .containsExactly(
            entry("serviceRating", Set.of("serviceRating.required"))
        );

    assertThat(extractedErrorMessages)
        .containsExactly(
            entry("serviceRating", Set.of("Select how you felt about this service"))
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "invalid value")
  void validate_whenInvalidForm_thenValidationErrors(String invalidValue) {
    var form = FeedbackFormTestUtil.builder()
        .withServiceRating(invalidValue)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    feedbackFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    var extractedErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(extractedErrors)
        .containsExactly(
            entry("serviceRating", Set.of("serviceRating.required"))
        );

    assertThat(extractedErrorMessages)
        .containsExactly(
            entry("serviceRating", Set.of("Select how you felt about this service"))
        );
  }

  @Test
  void validate_feedbackInput_characterCountOverMax() {
    var overCharacterLimit = StringUtils.repeat("a", FeedbackController.MAX_FEEDBACK_CHARACTER_LENGTH + 1);

    var form = FeedbackFormTestUtil.builder()
        .withServiceRating(ServiceFeedbackRating.VERY_SATISFIED.name())
        .withFeedback(overCharacterLimit)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    feedbackFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    var extractedErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(extractedErrors)
        .containsExactly(
            entry("feedback.inputValue", Set.of("feedback.invalid"))
        );

    assertThat(extractedErrorMessages)
        .containsExactly(
            entry(
                "feedback.inputValue",
                Set.of("Feedback must contain %s characters or fewer".formatted(FeedbackController.MAX_FEEDBACK_CHARACTER_LENGTH))
            )
        );
  }

  @Test
  void validate_feedbackInput_characterCountUnderMaxWithNewLine() {
    var halfCharacterCount = FeedbackController.MAX_FEEDBACK_CHARACTER_LENGTH / 2;
    var halfCharacterList = StringUtils.repeat("a", halfCharacterCount);
    var feedback = "%s\n%s".formatted(halfCharacterList, halfCharacterList);

    var form = FeedbackFormTestUtil.builder()
        .withServiceRating(ServiceFeedbackRating.VERY_SATISFIED.name())
        .withFeedback(feedback)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    feedbackFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  private static class NonSupportedClass {
  }
}