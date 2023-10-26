package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

class InstallationInclusionForm {

  private String includeInstallationsInNomination;

  public String getIncludeInstallationsInNomination() {
    return includeInstallationsInNomination;
  }

  public void setIncludeInstallationsInNomination(String includeInstallationsInNomination) {
    this.includeInstallationsInNomination = includeInstallationsInNomination;
  }

  @Override
  public String toString() {
    return "InstallationInclusionForm{" +
        "includeInstallationsInNomination=" + includeInstallationsInNomination +
        '}';
  }
}
