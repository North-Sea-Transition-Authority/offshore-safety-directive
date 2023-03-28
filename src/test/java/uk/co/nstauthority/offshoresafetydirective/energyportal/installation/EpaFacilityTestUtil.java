package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import uk.co.fivium.energyportalapi.generated.types.Facility;
import uk.co.fivium.energyportalapi.generated.types.FacilityStatus;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class EpaFacilityTestUtil {

  private EpaFacilityTestUtil() {
    throw new IllegalUtilClassInstantiationException(getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Integer id = 25;

    private String name = "facility name";

    private FacilityType type = FacilityType.ANCHOR_LEG_MOORING;

    private FacilityStatus status = FacilityStatus.OPERATIONAL;

    private Boolean isInUkcs = true;

    Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    Builder withId(InstallationId installationId) {
      return withId(installationId.id());
    }

    Builder withName(String name) {
      this.name = name;
      return this;
    }

    Builder withType(FacilityType facilityType) {
      this.type = facilityType;
      return this;
    }

    Builder withStatus(FacilityStatus facilityStatus) {
      this.status = facilityStatus;
      return this;
    }

    Builder withInUkcs(Boolean isInUkcs) {
      this.isInUkcs = isInUkcs;
      return this;
    }

    Facility build() {
      return new Facility(
          id,
          name,
          type,
          status,
          isInUkcs
      );
    }

  }
}
