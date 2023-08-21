package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;

public record PortalOrganisationGroupDto(
    String organisationGroupId,
    String name
) {

  public static PortalOrganisationGroupDto fromOrganisationGroup(OrganisationGroup organisationGroup) {
    return new PortalOrganisationGroupDto(
        organisationGroup.getOrganisationGroupId().toString(),
        organisationGroup.getName()
    );
  }
}
