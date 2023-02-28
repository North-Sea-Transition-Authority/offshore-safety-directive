package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

public enum PortalAssetType {

  WELLBORE("Wellbore", "wellbore"),
  INSTALLATION("Installation", "installation"),
  SUBAREA("Subarea", "subarea");

  private final String displayName;

  private final String sentenceCaseDisplayName;

  PortalAssetType(String displayName, String sentenceCaseDisplayName) {
    this.displayName = displayName;
    this.sentenceCaseDisplayName = sentenceCaseDisplayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getSentenceCaseDisplayName() {
    return sentenceCaseDisplayName;
  }
}
