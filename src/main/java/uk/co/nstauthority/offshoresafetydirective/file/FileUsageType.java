package uk.co.nstauthority.offshoresafetydirective.file;

public enum FileUsageType {

  CASE_EVENT("CASE-EVENT")
  ;

  private final String usageType;

  FileUsageType(String usageType) {
    this.usageType = usageType;
  }

  public String getUsageType() {
    return usageType;
  }
}
