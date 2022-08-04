package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

public class InstallationAdviceFormTestUtil {

  private InstallationAdviceFormTestUtil() {
    throw new IllegalStateException("InstallationAdviceFormTestUtil is a util class and should not be instantiated");
  }

  static class InstallationAdviceFormBuilder {
    private Boolean includeInstallationsInNomination = true;

    InstallationAdviceFormBuilder includeInstallationsInNomination(Boolean includeInstallationsInNomination) {
      this.includeInstallationsInNomination = includeInstallationsInNomination;
      return this;
    }

    InstallationAdviceForm build() {
      return new InstallationAdviceForm()
          .setIncludeInstallationsInNomination(includeInstallationsInNomination);
    }
  }
}
