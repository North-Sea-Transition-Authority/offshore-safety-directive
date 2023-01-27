package uk.co.nstauthority.offshoresafetydirective.file;

public enum VirtualFolder {
  SUPPORTING_DOCUMENTS("supporting-documents"),
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
