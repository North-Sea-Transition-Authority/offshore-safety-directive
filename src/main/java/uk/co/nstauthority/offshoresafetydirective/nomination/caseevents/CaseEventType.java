package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

public enum CaseEventType {

  QA_CHECKS("QA checks completed"),
  DECISION("Decision");

  private final String screenDisplayText;

  CaseEventType(String screenDisplayText) {
    this.screenDisplayText = screenDisplayText;
  }

  public String getScreenDisplayText() {
    return screenDisplayText;
  }
}
