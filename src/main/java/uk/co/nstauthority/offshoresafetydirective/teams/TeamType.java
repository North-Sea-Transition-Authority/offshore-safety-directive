package uk.co.nstauthority.offshoresafetydirective.teams;

public enum TeamType {

  REGULATOR("Regulator", 10),
  CONSULTEE("Consultee", 20);

  private final String displayText;
  private final int displayOrder;

  TeamType(String displayText, int displayOrder) {
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