package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class InstallationInclusionTestUtil {

  private InstallationInclusionTestUtil() {
    throw new IllegalStateException("InstallationInclusionTestUtil is a util class and should not be instantiated");
  }

  static class InstallationInclusionBuilder {
    private NominationDetail nominationDetail = NominationDetailTestUtil.getNominationDetail();
    private Boolean includeInstallationsInNomination = true;

    InstallationInclusionBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    InstallationInclusionBuilder includeInstallationsInNomination(Boolean includeInstallationsInNomination) {
      this.includeInstallationsInNomination = includeInstallationsInNomination;
      return this;
    }

    InstallationInclusion build() {
      return new InstallationInclusion()
          .setNominationDetail(nominationDetail)
          .setIncludeInstallationsInNomination(includeInstallationsInNomination);
    }
  }
}
