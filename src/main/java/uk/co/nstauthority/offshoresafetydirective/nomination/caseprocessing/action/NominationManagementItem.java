package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

public enum NominationManagementItem {

  QA_CHECKS("Complete QA checks", 10),
  NOMINATION_DECISION("Record decision", 20),
  WITHDRAW("Withdraw nomination", 30),
  CONFIRM_APPOINTMENT("Confirm appointment", 40),
  GENERAL_CASE_NOTE("Add a case note", 50),
  PEARS_REFERENCE("Update related PEARS applications", 60),
  WONS_REFERENCE("Update related WONS applications", 70);

  private final String actionText;
  private final int displayOrder;

  NominationManagementItem(String actionText, int displayOrder) {
    this.actionText = actionText;
    this.displayOrder = displayOrder;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getActionText() {
    return actionText;
  }
}
