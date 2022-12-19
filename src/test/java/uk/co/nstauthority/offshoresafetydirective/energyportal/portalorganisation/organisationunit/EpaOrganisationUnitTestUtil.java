package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.time.LocalDate;
import java.util.Collections;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class EpaOrganisationUnitTestUtil {

  private EpaOrganisationUnitTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

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

    Builder withId(Integer id) {
      this.organisationUnitId = id;
      return this;
    }

    Builder withName(String name) {
      this.name = name;
      return this;
    }

    Builder withShortName(String shortName) {
      this.shortName = shortName;
      return this;
    }

    Builder withRegisteredNumber(String registeredNumber) {
      this.registeredNumber = registeredNumber;
      return this;
    }

    Builder withForeignRegisteredNumber(String foreignRegisteredNumber) {
      this.foreignRegisteredNumber = foreignRegisteredNumber;
      return this;
    }

    Builder withForeignRegisteredName(String foreignRegisteredName) {
      this.foreignRegisteredName = foreignRegisteredName;
      return this;
    }

    Builder withCountryOfOrigin(String countryOfOrigin) {
      this.countryOfOrigin = countryOfOrigin;
      return this;
    }

    Builder withOriginCountryCode(Integer originCountryCode) {
      this.originCountryCode = originCountryCode;
      return this;
    }

    Builder withStartDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    Builder withEndDate(LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    Builder isDuplicate(Boolean isDuplicate) {
      this.isDuplicate = isDuplicate;
      return this;
    }

    Builder isActive(Boolean isActive) {
      this.isActive = isActive;
      return this;
    }

    OrganisationUnit build() {
      return new OrganisationUnit(
          organisationUnitId,
          name,
          shortName,
          registeredNumber,
          foreignRegisteredNumber,
          foreignRegisteredName,
          countryOfOrigin,
          originCountryCode,
          startDate,
          endDate,
          isDuplicate,
          isActive,
          Collections.emptyList()
      );
    }
  }
}
