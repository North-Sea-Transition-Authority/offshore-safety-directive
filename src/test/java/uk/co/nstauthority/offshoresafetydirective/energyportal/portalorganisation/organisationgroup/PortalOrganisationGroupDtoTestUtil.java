package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroupEmailDomain;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
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

    private Set<PortalOrganisationDto> organisations = new HashSet<>();
    private List<OrganisationGroupEmailDomain> emailDomains = new ArrayList<>();

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

    public Builder withOrganisationGroupId(Integer organisationGroupId) {
      this.organisationGroupId = String.valueOf(organisationGroupId);
      return this;
    }

    public Builder withOrganisation(PortalOrganisationDto organisation) {
      organisations.add(organisation);
      return this;
    }

    public Builder withOrganisations(Set<PortalOrganisationDto> organisations) {
      this.organisations = organisations;
      return this;
    }

    public Builder withEmailDomains(OrganisationGroupEmailDomain emails) {
      emailDomains.add(emails);
      return this;
    }

    public PortalOrganisationGroupDto build() {
      return new PortalOrganisationGroupDto(
          organisationGroupId,
          name,
          organisations,
          emailDomains
      );
    }

  }

}
