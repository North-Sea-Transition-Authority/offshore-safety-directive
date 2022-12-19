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

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public PortalOrganisationDto build() {
      return new PortalOrganisationDto(
          id,
          name
      );
    }

  }

}