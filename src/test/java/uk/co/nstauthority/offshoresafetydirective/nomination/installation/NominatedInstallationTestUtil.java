package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class NominatedInstallationTestUtil {

  private NominatedInstallationTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static NominatedInstallationBuilder builder() {
    return new NominatedInstallationBuilder();
  }

  public static class NominatedInstallationBuilder {
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Integer installationId = 1;

    public NominatedInstallationBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public NominatedInstallationBuilder withInstallationId(Integer installationId) {
      this.installationId = installationId;
      return this;
    }

    public NominatedInstallation build() {
      return new NominatedInstallation()
          .setNominationDetail(nominationDetail)
          .setInstallationId(installationId);
    }
  }
}
