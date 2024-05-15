package uk.co.nstauthority.offshoresafetydirective.file;

public enum FileUsageType {

  CASE_EVENT("CASE-EVENT"),
  NOMINATION_DETAIL("NOMINATION-DETAIL"),
  TERMINATION("TERMINATION")
  ;

  private final String usageType;

  FileUsageType(String usageType) {
    this.usageType = usageType;
  }

  public String getUsageType() {
    return usageType;
  }
}
