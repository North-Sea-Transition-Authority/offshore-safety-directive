package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

public enum CaseProcessingActionGroup {

  ADD_CASE_NOTE("Add a case note", 10),
  COMPLETE_QA_CHECKS("Complete QA checks", 20),
  REQUEST_UPDATE("Request update", 30),
  CONSULTATIONS("Consultations", 40),
  DECISION("Decision", 50),
  RELATED_APPLICATIONS("Related applications", 60),
  CONFIRM_APPOINTMENT("Confirm appointment", 70)
  ;

  private final String displayText;
  private final int displayOrder;

  CaseProcessingActionGroup(String displayText, int displayOrder) {
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
