package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

public enum NominationDecision {

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

  public int getDisplayOrder() {
    return displayOrder;
  }
}
