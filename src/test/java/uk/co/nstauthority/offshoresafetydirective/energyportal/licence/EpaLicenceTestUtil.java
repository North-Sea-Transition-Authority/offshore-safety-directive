package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import java.time.LocalDate;
import uk.co.fivium.energyportalapi.generated.types.Licence;
import uk.co.fivium.energyportalapi.generated.types.LicenceStatus;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class EpaLicenceTestUtil {

  private EpaLicenceTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 1000;

    private String licenceType = "licence type";

    private Integer licenceNumber = 20;

    private String licenceReference = "licence reference";

    private LocalDate licenceStartDate = LocalDate.now();

    private LocalDate licenceEndDate;

    private LicenceStatus licenceStatus = LicenceStatus.EXTANT;

    private Builder() {}

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withLicenceType(String licenceType) {
      this.licenceType = licenceType;
      return this;
    }

    Builder withLicenceNumber(Integer licenceNumber) {
      this.licenceNumber = licenceNumber;
      return this;
    }

    Builder withLicenceReference(String licenceReference) {
      this.licenceReference = licenceReference;
      return this;
    }

    public Builder withStartDate(LocalDate licenceStartDate) {
      this.licenceStartDate = licenceStartDate;
      return this;
    }

    public Builder withEndDate(LocalDate licenceEndDate) {
      this.licenceEndDate = licenceEndDate;
      return this;
    }

    public Builder withStatus(LicenceStatus licenceStatus) {
      this.licenceStatus = licenceStatus;
      return this;
    }

    public Licence build() {
      return Licence.newBuilder()
          .id(id)
          .licenceType(licenceType)
          .licenceNo(licenceNumber)
          .licenceRef(licenceReference)
          .licenceStartDate(licenceStartDate)
          .licenceEndDate(licenceEndDate)
          .licenceStatus(licenceStatus)
          .build();
    }

  }
}
