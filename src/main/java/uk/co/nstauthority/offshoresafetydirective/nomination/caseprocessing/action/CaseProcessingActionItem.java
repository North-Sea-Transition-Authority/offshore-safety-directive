package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

import uk.co.nstauthority.offshoresafetydirective.displayableutil.Displayable;

public enum CaseProcessingActionItem implements Displayable {

  UPDATE_NOMINATION("Update nomination", 5, CaseProcessingActionIdentifier.REQUEST_UPDATE),
  GENERAL_CASE_NOTE("Add a case note", 10, CaseProcessingActionIdentifier.GENERAL_NOTE),
  QA_CHECKS("Complete QA checks", 20, CaseProcessingActionIdentifier.QA),
  REQUEST_UPDATE("Request update", 30, CaseProcessingActionIdentifier.REQUEST_UPDATE),
  NOMINATION_DECISION("Record decision", 40, CaseProcessingActionIdentifier.DECISION),
  SEND_FOR_CONSULTATION("Send for consultation", 50, CaseProcessingActionIdentifier.SEND_FOR_CONSULTATION),
  CONSULTATION_RESPONSE("Add consultation response", 60, CaseProcessingActionIdentifier.CONSULTATION_RESPONSE),
  WITHDRAW("Withdraw nomination", 70, CaseProcessingActionIdentifier.WITHDRAW),
  CONFIRM_APPOINTMENT("Confirm appointment", 80, CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT),
  PEARS_REFERENCE("Update related PEARS applications", 90, CaseProcessingActionIdentifier.PEARS_REFERENCES),
  WONS_REFERENCE("Update related WONS applications", 100, CaseProcessingActionIdentifier.WONS_REFERENCES),
  CONTACT_ORGANISATION("Contact organisation", 110, CaseProcessingActionIdentifier.CONTACT_ORGANISATION);

  private final String actionText;
  private final int displayOrder;
  private final CaseProcessingActionIdentifier identifier;

  CaseProcessingActionItem(String actionText, int displayOrder,
                           String identifier) {
    this.actionText = actionText;
    this.displayOrder = displayOrder;
    this.identifier = new CaseProcessingActionIdentifier(identifier);
  }

  @Override
  public String getDisplayName() {
    return actionText;
  }

  @Override
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
