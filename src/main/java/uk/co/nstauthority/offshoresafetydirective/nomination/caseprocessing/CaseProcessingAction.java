package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

public record CaseProcessingAction(String value) {

  public static final String DECISION = "DECISION";
  public static final String QA = "QA";
  public static final String WITHDRAW = "WITHDRAW";
  public static final String CONFIRM_APPOINTMENT = "CONFIRM_APPOINTMENT";
  public static final String GENERAL_NOTE = "GENERAL_CASE_NOTE";
  public static final String PEARS_REFERENCES = "PEARS_REFERENCES";
  public static final String WONS_REFERENCES = "WONS_REFERENCES";

}
