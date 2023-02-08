package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class LicenceDtoTestUtil {

  private LicenceDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String licenceType = "licence type";

    private Integer licenceNumber = 20;

    private String licenceReference = "licence reference";

    private Builder() {}

    public Builder withLicenceType(String licenceType) {
      this.licenceType = licenceType;
      return this;
    }

    Builder withLicenceNumber(Integer licenceNumber) {
      this.licenceNumber = licenceNumber;
      return this;
    }

    public Builder withLicenceReference(String licenceReference) {
      this.licenceReference = licenceReference;
      return this;
    }

    public LicenceDto build() {
      return new LicenceDto(
          new LicenceDto.LicenceType(licenceType),
          new LicenceDto.LicenceNumber(licenceNumber),
          new LicenceDto.LicenceReference(licenceReference)
      );
    }
  }
}
