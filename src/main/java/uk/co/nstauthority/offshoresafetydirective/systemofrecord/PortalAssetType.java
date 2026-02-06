package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import uk.co.nstauthority.offshoresafetydirective.displayableutil.Displayable;

public enum PortalAssetType implements Displayable {

  WELLBORE("Wellbore", "wellbore"),
  INSTALLATION("Installation", "installation"),
  SUBAREA("Subarea", "subarea");

  private final String displayName;

  private final String sentenceCaseDisplayName;

  PortalAssetType(String displayName, String sentenceCaseDisplayName) {
    this.displayName = displayName;
    this.sentenceCaseDisplayName = sentenceCaseDisplayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  public String getSentenceCaseDisplayName() {
    return sentenceCaseDisplayName;
  }
}
