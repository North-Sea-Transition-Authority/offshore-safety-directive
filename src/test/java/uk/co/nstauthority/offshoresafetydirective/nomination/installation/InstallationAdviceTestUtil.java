package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class InstallationAdviceTestUtil {

  private InstallationAdviceTestUtil() {
    throw new IllegalStateException("InstallationAdviceTestUtil is a util class and should not be instantiated");
  }

  static class InstallationAdviceBuilder {
    private NominationDetail nominationDetail = NominationDetailTestUtil.getNominationDetail();
    private Boolean includeInstallationsInNomination = true;

    InstallationAdviceBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    InstallationAdviceBuilder includeInstallationsInNomination(Boolean includeInstallationsInNomination) {
      this.includeInstallationsInNomination = includeInstallationsInNomination;
      return this;
    }

    InstallationAdvice build() {
      return new InstallationAdvice()
          .setNominationDetail(nominationDetail)
          .setIncludeInstallationsInNomination(includeInstallationsInNomination);
    }
  }
}
