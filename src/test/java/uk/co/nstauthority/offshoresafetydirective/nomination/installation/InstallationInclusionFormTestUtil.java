package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

public class InstallationInclusionFormTestUtil {

  private InstallationInclusionFormTestUtil() {
    throw new IllegalStateException("InstallationInclusionFormTestUtil is a util class and should not be instantiated");
  }

  static class InstallationInclusionFormBuilder {
    private String includeInstallationsInNomination = "true";

    InstallationInclusionFormBuilder includeInstallationsInNomination(Boolean includeInstallationsInNomination) {
      this.includeInstallationsInNomination = String.valueOf(includeInstallationsInNomination);
      return this;
    }

    InstallationInclusionFormBuilder includeInstallationsInNomination(String includeInstallationsInNomination) {
      this.includeInstallationsInNomination = includeInstallationsInNomination;
      return this;
    }

    InstallationInclusionForm build() {
      var form =  new InstallationInclusionForm();
      form.setIncludeInstallationsInNomination(includeInstallationsInNomination);
      return form;
    }
  }
}
