package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

public enum CaseProcessingActionItem {

  QA_CHECKS("Complete QA checks", 10, CaseProcessingActionIdentifier.QA),
  NOMINATION_DECISION("Record decision", 20, CaseProcessingActionIdentifier.DECISION),
  WITHDRAW("Withdraw nomination", 30, CaseProcessingActionIdentifier.WITHDRAW),
  CONFIRM_APPOINTMENT("Confirm appointment", 40, CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT),
  GENERAL_CASE_NOTE("Add a case note", 50, CaseProcessingActionIdentifier.GENERAL_NOTE),
  PEARS_REFERENCE("Update related PEARS applications", 60, CaseProcessingActionIdentifier.PEARS_REFERENCES),
  WONS_REFERENCE("Update related WONS applications", 70, CaseProcessingActionIdentifier.WONS_REFERENCES);

  private final String actionText;
  private final int displayOrder;
  private final CaseProcessingActionIdentifier identifier;

  CaseProcessingActionItem(String actionText, int displayOrder,
                           String identifier) {
    this.actionText = actionText;
    this.displayOrder = displayOrder;
    this.identifier = new CaseProcessingActionIdentifier(identifier);
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getActionText() {
    return actionText;
  }

  public CaseProcessingActionIdentifier getIdentifier() {
    return identifier;
  }
}
