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

    private FacilityType type = InstallationQueryService.ALLOWED_INSTALLATION_TYPES.get(0);

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

    public InstallationDto build() {
      return new InstallationDto(id, name, type);
    }
  }
}
