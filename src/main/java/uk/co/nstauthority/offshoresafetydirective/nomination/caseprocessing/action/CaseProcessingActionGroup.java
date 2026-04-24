package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

import uk.co.nstauthority.offshoresafetydirective.displayableutil.Displayable;

public enum CaseProcessingActionGroup implements Displayable {

  UPDATE_NOMINATION("Update nomination", 5),
  ADD_CASE_NOTE("Add a case note", 10),
  COMPLETE_QA_CHECKS("Complete QA checks", 20),
  REQUEST_UPDATE("Request update", 30),
  CONSULTATIONS("Consultations", 40),
  DECISION("Decision", 50),
  RELATED_APPLICATIONS("Related applications", 60),
  CONFIRM_APPOINTMENT("Confirm appointment", 70),
  CONTACT_ORGANISATION("Contact organisation", 80),
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

  @Override
  public String getDisplayName() {
    return displayText;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }
}
