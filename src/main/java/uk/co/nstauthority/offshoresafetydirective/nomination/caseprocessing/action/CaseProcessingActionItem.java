package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

public enum CaseProcessingActionItem {

  GENERAL_CASE_NOTE("Add a case note", 10, CaseProcessingActionIdentifier.GENERAL_NOTE),
  QA_CHECKS("Complete QA checks", 20, CaseProcessingActionIdentifier.QA),
  NOMINATION_DECISION("Record decision", 30, CaseProcessingActionIdentifier.DECISION),
  SEND_FOR_CONSULTATION("Send for consultation", 40, CaseProcessingActionIdentifier.SEND_FOR_CONSULTATION),
  CONSULTATION_RESPONSE("Add consultation response", 50, CaseProcessingActionIdentifier.CONSULTATION_RESPONSE),
  WITHDRAW("Withdraw nomination", 60, CaseProcessingActionIdentifier.WITHDRAW),
  CONFIRM_APPOINTMENT("Confirm appointment", 70, CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT),
  PEARS_REFERENCE("Update related PEARS applications", 80, CaseProcessingActionIdentifier.PEARS_REFERENCES),
  WONS_REFERENCE("Update related WONS applications", 90, CaseProcessingActionIdentifier.WONS_REFERENCES);

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
