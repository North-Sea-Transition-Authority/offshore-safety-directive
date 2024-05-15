package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
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

    private WellboreMechanicalStatus mechanicalStatus;

    private LicenceDto originLicenceDto = LicenceDtoTestUtil.builder()
        .withLicenceReference("P101")
        .build();

    private LicenceDto totalDepthLicenceDto = LicenceDtoTestUtil.builder()
        .withLicenceReference("P123")
        .build();

    public Builder withWellboreId(Integer wellboreId) {
      this.wellboreId = new WellboreId(wellboreId);
      return this;
    }

    public Builder withRegistrationNumber(String registrationNumber) {
      this.registrationNumber = registrationNumber;
      return this;
    }

    public Builder withMechanicalStatus(WellboreMechanicalStatus wellboreMechanicalStatus) {
      this.mechanicalStatus = wellboreMechanicalStatus;
      return this;
    }

    public Builder withOriginLicenceDto(
        LicenceDto originLicenceDto) {
      this.originLicenceDto = originLicenceDto;
      return this;
    }

    public Builder withTotalDepthLicenceDto(LicenceDto totalDepthLicenceDto) {
      this.totalDepthLicenceDto = totalDepthLicenceDto;
      return this;
    }

    public WellDto build() {

      return new WellDto(
          wellboreId,
          registrationNumber,
          mechanicalStatus,
          originLicenceDto,
          totalDepthLicenceDto
      );
    }
  }
}