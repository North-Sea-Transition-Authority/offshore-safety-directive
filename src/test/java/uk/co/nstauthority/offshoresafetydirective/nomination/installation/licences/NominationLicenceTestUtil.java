package uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class NominationLicenceTestUtil {

  private NominationLicenceTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private UUID id = UUID.randomUUID();
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Integer licenceId = 1;

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public Builder withLicenceId(Integer licenceId) {
      this.licenceId = licenceId;
      return this;
    }

    public NominationLicence build() {
        var nominationLicence = new NominationLicence(id);
      nominationLicence.setNominationDetail(nominationDetail);
      nominationLicence.setLicenceId(licenceId);
      return nominationLicence;
    }
  }
}
