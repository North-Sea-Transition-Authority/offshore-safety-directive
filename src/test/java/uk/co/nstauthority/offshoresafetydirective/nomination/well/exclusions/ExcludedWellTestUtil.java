package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class ExcludedWellTestUtil {

  private ExcludedWellTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID uuid = UUID.randomUUID();

    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();

    private int wellboreId = 200;

    private Builder() {}

    public Builder withUuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public Builder withWellboreId(int wellboreId) {
      this.wellboreId = wellboreId;
      return this;
    }

    public ExcludedWell build() {
      var excludedWell = new ExcludedWell(uuid);
      excludedWell.setNominationDetail(nominationDetail);
      excludedWell.setWellboreId(wellboreId);
      return excludedWell;
    }
  }
}
