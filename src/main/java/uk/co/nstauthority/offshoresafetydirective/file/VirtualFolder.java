package uk.co.nstauthority.offshoresafetydirective.file;

public enum VirtualFolder {
  NOMINATION_DECISION("case-events/nomination-decision"),
  CONFIRM_APPOINTMENTS("case-events/confirm-appointments"),
  CASE_NOTES("case-events/case-notes"),
  CONSULTATIONS("case-processing/consultations")
  ;

  private final String folderPath;

  VirtualFolder(String folderPath) {
    this.folderPath = folderPath;
  }

  @Override
  public String toString() {
    return folderPath;
  }
}
