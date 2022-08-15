package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

public class InstallationInclusionFormTestUtil {

  private InstallationInclusionFormTestUtil() {
    throw new IllegalStateException("InstallationInclusionFormTestUtil is a util class and should not be instantiated");
  }

  static class InstallationInclusionFormBuilder {
    private Boolean includeInstallationsInNomination = true;

    InstallationInclusionFormBuilder includeInstallationsInNomination(Boolean includeInstallationsInNomination) {
      this.includeInstallationsInNomination = includeInstallationsInNomination;
      return this;
    }

    InstallationInclusionForm build() {
      return new InstallationInclusionForm()
          .setIncludeInstallationsInNomination(includeInstallationsInNomination);
    }
  }
}
