package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

class InstallationInclusionForm {

  private Boolean includeInstallationsInNomination;

  public Boolean getIncludeInstallationsInNomination() {
    return includeInstallationsInNomination;
  }

  public InstallationInclusionForm setIncludeInstallationsInNomination(Boolean includeInstallationsInNomination) {
    this.includeInstallationsInNomination = includeInstallationsInNomination;
    return this;
  }

  @Override
  public String toString() {
    return "InstallationInclusionForm{" +
        "includeInstallationsInNomination=" + includeInstallationsInNomination +
        '}';
  }
}
