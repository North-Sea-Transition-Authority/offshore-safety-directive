package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class PortalOrganisationDtoTestUtil {

  private PortalOrganisationDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 100;
    private String name = "Name";
    private OrganisationRegisteredNumber registeredNumber = new OrganisationRegisteredNumber("registered number");
    private Boolean isActive = true;
    private Boolean isDuplicate = false;

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withRegisteredNumber(String registeredNumber) {
      return withRegisteredNumber(new OrganisationRegisteredNumber(registeredNumber));
    }

    public Builder withRegisteredNumber(OrganisationRegisteredNumber registeredNumber) {
      this.registeredNumber = registeredNumber;
      return this;
    }

    public Builder isActive(Boolean isActive) {
      this.isActive = isActive;
      return this;
    }

    public Builder isDuplicate(Boolean duplicate) {
      isDuplicate = duplicate;
      return this;
    }

    public PortalOrganisationDto build() {
      return new PortalOrganisationDto(
          id,
          name,
          registeredNumber,
          isActive,
          isDuplicate
      );
    }

  }

}