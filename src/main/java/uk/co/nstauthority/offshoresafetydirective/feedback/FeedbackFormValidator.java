package uk.co.nstauthority.offshoresafetydirective.feedback;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.nstauthority.offshoresafetydirective.validationutil.EnumValidationUtil;

@Service
public class FeedbackFormValidator implements Validator {


  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FeedbackForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    var form = (FeedbackForm) target;

    ValidationUtils.rejectIfEmpty(
        errors,
        "serviceRating",
        "serviceRating.required",
        "Select how you felt about this service");

    if (StringUtils.isNotBlank(form.getServiceRating())
        && !EnumValidationUtil.isValidEnumValue(ServiceFeedbackRating.class, form.getServiceRating())) {
      errors.rejectValue("serviceRating",
          "serviceRating.required",
          "Select how you felt about this service");
    }

    if (StringUtils.isNotBlank(form.getFeedback().getInputValue())
        && form.getFeedback().getInputValue().replace("\n", "").length() > FeedbackController.MAX_FEEDBACK_CHARACTER_LENGTH) {
      errors.rejectValue(
          "feedback.inputValue",
          "feedback.invalid",
          "Feedback must contain %s characters or fewer".formatted(FeedbackController.MAX_FEEDBACK_CHARACTER_LENGTH));
    }
  }
}
