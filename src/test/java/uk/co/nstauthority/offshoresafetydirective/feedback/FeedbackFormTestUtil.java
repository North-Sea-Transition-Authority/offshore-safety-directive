package uk.co.nstauthority.offshoresafetydirective.feedback;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class FeedbackFormTestUtil {

  private FeedbackFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String serviceRating = ServiceFeedbackRating.SATISFIED.name();
    private String feedback = "feedback";

    Builder withServiceRating(String serviceRating) {
      this.serviceRating = serviceRating;
      return this;
    }

    Builder withFeedback(String feedback) {
      this.feedback = feedback;
      return this;
    }

    FeedbackForm build() {
      var form = new FeedbackForm();
      form.setServiceRating(serviceRating);
      form.getFeedback().setInputValue(feedback);
      return form;
    }
  }
}
