package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class WellDtoTestUtil {

  private WellDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Builder() {}

    private WellboreId wellboreId = new WellboreId(25);

    private String registrationNumber = "registration number";

    private WonsWellboreIntent intent = WonsWellboreIntent.EXPLORATION;

    public Builder withWellboreId(Integer wellboreId) {
      this.wellboreId = new WellboreId(wellboreId);
      return this;
    }

    public Builder withRegistrationNumber(String registrationNumber) {
      this.registrationNumber = registrationNumber;
      return this;
    }

    public Builder withIntent(WonsWellboreIntent intent) {
      this.intent = intent;
      return this;
    }

    public WellDto build() {

      var mechanicalStatus = WellboreMechanicalStatus.PLANNED;

      var originLicenceDto = LicenceDtoTestUtil.builder()
          .withLicenceReference("P101")
          .build();

      var totalDepthLicenceDto = LicenceDtoTestUtil.builder()
          .withLicenceReference("P123")
          .build();

      return new WellDto(
          wellboreId,
          registrationNumber,
          mechanicalStatus,
          originLicenceDto,
          totalDepthLicenceDto,
          intent
      );
    }
  }
}