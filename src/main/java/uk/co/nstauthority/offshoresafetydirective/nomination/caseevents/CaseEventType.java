package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

public enum CaseEventType {

  QA_CHECKS("QA checks completed"),
  NO_OBJECTION_DECISION("No objections"),
  OBJECTION_DECISION("Objected"),
  WITHDRAWN("Withdrawn"),
  CONFIRM_APPOINTMENT("Confirm appointment"),
  GENERAL_NOTE("General note"),
  NOMINATION_SUBMITTED("Nomination submitted");

  private final String screenDisplayText;

  CaseEventType(String screenDisplayText) {
    this.screenDisplayText = screenDisplayText;
  }

  public String getScreenDisplayText() {
    return screenDisplayText;
  }
}
