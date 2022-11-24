package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;

public enum WellSelectionType implements DisplayableEnumOption {
  SPECIFIC_WELLS(10, "Yes, I want to provide specific wells"),
  LICENCE_BLOCK_SUBAREA(20, "Yes, I want to provide licence block subareas the nomination is for"),
  NO_WELLS(30, "No");

  private final int displayOrder;
  private final String screenDisplayText;

  WellSelectionType(int displayOrder, String screenDisplayText) {
    this.displayOrder = displayOrder;
    this.screenDisplayText = screenDisplayText;
  }

  @Override
  public String getScreenDisplayText() {
    return screenDisplayText;
  }

  @Override
  public String getFormValue() {
    return this.name();
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }


}
