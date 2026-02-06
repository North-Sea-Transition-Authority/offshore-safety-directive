package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import uk.co.nstauthority.offshoresafetydirective.displayableutil.Displayable;

public enum NominationDecision implements Displayable {

  NO_OBJECTION("No objection", 10),
  OBJECTION("Objection", 20);

  private final String displayText;
  private final int displayOrder;

  NominationDecision(String displayText, int displayOrder) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  @Override
  public String getDisplayName() {
    return displayText;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }
}
