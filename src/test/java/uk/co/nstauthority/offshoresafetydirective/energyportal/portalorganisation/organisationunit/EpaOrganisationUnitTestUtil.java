package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.time.LocalDate;
import java.util.Collections;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class EpaOrganisationUnitTestUtil {

  private EpaOrganisationUnitTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer organisationUnitId = 100;

    private String name = "organisation unit name";

    private String shortName = "organisation unit short name";

    private String registeredNumber = "organisation unit registered number";

    private String foreignRegisteredNumber = "foreign registered number";

    private String foreignRegisteredName = "foregin registered name";

    private String countryOfOrigin = "country of origin";

    private Integer originCountryCode = 75;

    private LocalDate startDate = LocalDate.now();

    private LocalDate endDate = null;

    private Boolean isDuplicate = false;

    private Boolean isActive = true;

    public Builder withId(Integer id) {
      this.organisationUnitId = id;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withShortName(String shortName) {
      this.shortName = shortName;
      return this;
    }

    public Builder withRegisteredNumber(String registeredNumber) {
      this.registeredNumber = registeredNumber;
      return this;
    }

    public Builder withForeignRegisteredNumber(String foreignRegisteredNumber) {
      this.foreignRegisteredNumber = foreignRegisteredNumber;
      return this;
    }

    public Builder withForeignRegisteredName(String foreignRegisteredName) {
      this.foreignRegisteredName = foreignRegisteredName;
      return this;
    }

    public Builder withCountryOfOrigin(String countryOfOrigin) {
      this.countryOfOrigin = countryOfOrigin;
      return this;
    }

    public Builder withOriginCountryCode(Integer originCountryCode) {
      this.originCountryCode = originCountryCode;
      return this;
    }

    public Builder withStartDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    public Builder withEndDate(LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    public Builder isDuplicate(Boolean isDuplicate) {
      this.isDuplicate = isDuplicate;
      return this;
    }

    public Builder isActive(Boolean isActive) {
      this.isActive = isActive;
      return this;
    }

    public OrganisationUnit build() {

      return OrganisationUnit.newBuilder()
          .organisationUnitId(organisationUnitId)
          .name(name)
          .shortName(shortName)
          .registeredNumber(registeredNumber)
          .foreignRegisteredNumber(foreignRegisteredNumber)
          .foreignRegisteredName(foreignRegisteredName)
          .countryOfOrigin(countryOfOrigin)
          .originCountryCode(originCountryCode)
          .startDate(startDate)
          .endDate(endDate)
          .isDuplicate(isDuplicate)
          .isActive(isActive)
          .organisationGroups(Collections.emptyList())
          .build();
    }
  }
}
