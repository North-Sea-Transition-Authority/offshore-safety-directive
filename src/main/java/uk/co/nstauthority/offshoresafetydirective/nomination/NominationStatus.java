package uk.co.nstauthority.offshoresafetydirective.nomination;

import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnum;

public enum NominationStatus implements DisplayableEnum {
  DRAFT("Draft", 10),
  SUBMITTED("Submitted", 20),
  DELETED("Deleted", 30);

  private final String displayText;
  private final Integer displayOrder;

  NominationStatus(String displayText, Integer displayOrder) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public String getScreenDisplayText() {
    return displayText;
  }

}
