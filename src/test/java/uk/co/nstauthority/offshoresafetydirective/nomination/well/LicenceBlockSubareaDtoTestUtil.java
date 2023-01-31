package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class LicenceBlockSubareaDtoTestUtil {

  private LicenceBlockSubareaDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private LicenceBlockSubareaId subareaId = new LicenceBlockSubareaId("subarea-id");
    private String subareaName = "subarea name";
    private String sortKey = "subarea sort key";

    Builder withSubareaId(String subareaId) {
      this.subareaId = new LicenceBlockSubareaId(subareaId);
      return this;
    }

    Builder withSubareaId(LicenceBlockSubareaId subareaId) {
      this.subareaId = subareaId;
      return this;
    }

    Builder withSubareaName(String subareaName) {
      this.subareaName = subareaName;
      return this;
    }

    Builder withSortKey(String sortKey) {
      this.sortKey = sortKey;
      return this;
    }

    LicenceBlockSubareaDto build() {
      return new LicenceBlockSubareaDto(
          subareaId,
          subareaName,
          sortKey
      );
    }

  }


}
