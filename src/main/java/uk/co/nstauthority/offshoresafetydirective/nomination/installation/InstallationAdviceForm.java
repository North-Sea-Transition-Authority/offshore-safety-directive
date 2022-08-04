package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

class InstallationAdviceForm {

  private Boolean includeInstallationsInNomination;

  public Boolean getIncludeInstallationsInNomination() {
    return includeInstallationsInNomination;
  }

  public InstallationAdviceForm setIncludeInstallationsInNomination(Boolean includeInstallationsInNomination) {
    this.includeInstallationsInNomination = includeInstallationsInNomination;
    return this;
  }

  @Override
  public String toString() {
    return "InstallationAdviceForm{" +
        "includeInstallationsInNomination=" + includeInstallationsInNomination +
        '}';
  }
}
