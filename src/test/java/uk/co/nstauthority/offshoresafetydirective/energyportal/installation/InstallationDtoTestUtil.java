package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class InstallationDtoTestUtil {

  private InstallationDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 1000;

    private String name = "Facility name";

    private FacilityType type = FacilityType.FLOATING_SEMI_SUBMERSIBLE_PROCESSING_UNIT;

    private boolean isInUkcs = true;

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withType(FacilityType facilityType) {
      this.type = facilityType;
      return this;
    }

    public Builder isInUkcs(boolean isInUkcs) {
      this.isInUkcs = isInUkcs;
      return this;
    }

    public InstallationDto build() {
      return new InstallationDto(
          id,
          name,
          type,
          isInUkcs
      );
    }
  }
}
