package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

public class InstallationInclusionView {

  private final Boolean includeInstallationsInNomination;

  public InstallationInclusionView() {
    this(null);
  }

  public InstallationInclusionView(Boolean includeInstallationsInNomination) {
    this.includeInstallationsInNomination = includeInstallationsInNomination;
  }

  public Boolean getIncludeInstallationsInNomination() {
    return includeInstallationsInNomination;
  }
}
