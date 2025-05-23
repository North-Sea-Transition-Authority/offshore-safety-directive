package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class InstallationInclusionTestUtil {

  private InstallationInclusionTestUtil() {
    throw new IllegalStateException("InstallationInclusionTestUtil is a util class and should not be instantiated");
  }

  public static InstallationInclusionBuilder builder() {
    return new InstallationInclusionBuilder();
  }

  public static class InstallationInclusionBuilder {
    private UUID id = UUID.randomUUID();
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Boolean includeInstallationsInNomination = true;

    public InstallationInclusionBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public InstallationInclusionBuilder includeInstallationsInNomination(Boolean includeInstallationsInNomination) {
      this.includeInstallationsInNomination = includeInstallationsInNomination;
      return this;
    }

    public InstallationInclusionBuilder withId(UUID id) {
      this.id = id;
      return this;
    }

    public InstallationInclusion build() {
      var installationInclusion = new InstallationInclusion(id);
      installationInclusion.setNominationDetail(nominationDetail);
      installationInclusion.setIncludeInstallationsInNomination(includeInstallationsInNomination);
      return installationInclusion;
    }
  }
}
