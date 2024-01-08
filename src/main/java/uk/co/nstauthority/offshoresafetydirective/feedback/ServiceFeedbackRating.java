package uk.co.nstauthority.offshoresafetydirective.feedback;

import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;

enum ServiceFeedbackRating implements DisplayableEnumOption {
  VERY_SATISFIED(10, "Very satisfied"),
  SATISFIED(20, "Satisfied"),
  NEITHER(30, "Neither satisfied or dissatisfied"),
  DISSATISFIED(40, "Dissatisfied"),
  VERY_DISSATISFIED(50, "Very dissatisfied");

  private final int displayOrder;
  private final String screenDisplayText;

  ServiceFeedbackRating(int displayOrder, String screenDisplayText) {
    this.displayOrder = displayOrder;
    this.screenDisplayText = screenDisplayText;
  }

  @Override
  public String getScreenDisplayText() {
    return screenDisplayText;
  }

  public String getFormValue() {
    return this.name();
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }
}
