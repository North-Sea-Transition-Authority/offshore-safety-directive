package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class ExcludedWellDetailTestUtil {

  private ExcludedWellDetailTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private UUID uuid = UUID.randomUUID();

    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();

    private Boolean hasWellsToExclude = true;

    private Builder() {}

    Builder withUuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    Builder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    Builder hasWellsToExclude(Boolean hasWellsToExclude) {
      this.hasWellsToExclude = hasWellsToExclude;
      return this;
    }

    ExcludedWellDetail build() {
      var excludedWellDetail = new ExcludedWellDetail(uuid);
      excludedWellDetail.setNominationDetail(nominationDetail);
      excludedWellDetail.setHasWellsToExclude(hasWellsToExclude);
      return excludedWellDetail;
    }
  }
}
