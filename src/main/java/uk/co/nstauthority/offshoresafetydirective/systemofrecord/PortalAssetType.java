package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

public enum PortalAssetType {

  WELLBORE("Wellbore"),
  INSTALLATION("Installation"),
  SUBAREA("Subarea");

  private final String displayName;

  PortalAssetType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
