package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class PortalOrganisationGroupDtoTestUtil {

  private PortalOrganisationGroupDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String name = "Organisation name";
    private String organisationGroupId = UUID.randomUUID().toString();

    private Builder() {
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withOrganisationGroupId(String organisationGroupId) {
      this.organisationGroupId = organisationGroupId;
      return this;
    }

    public PortalOrganisationGroupDto build() {
      return new PortalOrganisationGroupDto(
          organisationGroupId,
          name
      );
    }

  }

}
