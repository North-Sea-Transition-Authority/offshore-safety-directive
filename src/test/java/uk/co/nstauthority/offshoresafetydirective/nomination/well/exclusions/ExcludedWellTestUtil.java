package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class ExcludedWellTestUtil {

  private ExcludedWellTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private UUID uuid = UUID.randomUUID();

    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();

    private int wellboreId = 200;

    private Builder() {}

    Builder withUuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    Builder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    Builder withWellboreId(int wellboreId) {
      this.wellboreId = wellboreId;
      return this;
    }

    ExcludedWell build() {
      var excludedWell = new ExcludedWell(uuid);
      excludedWell.setNominationDetail(nominationDetail);
      excludedWell.setWellboreId(wellboreId);
      return excludedWell;
    }
  }
}
