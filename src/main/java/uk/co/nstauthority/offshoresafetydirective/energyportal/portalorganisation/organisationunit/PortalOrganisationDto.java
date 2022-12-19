package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;

public record PortalOrganisationDto(Integer id, String name, OrganisationRegisteredNumber registeredNumber) {

  static PortalOrganisationDto fromOrganisationUnit(OrganisationUnit organisationUnit) {
    return new PortalOrganisationDto(
        organisationUnit.getOrganisationUnitId(),
        organisationUnit.getName(),
        new OrganisationRegisteredNumber(organisationUnit.getRegisteredNumber())
    );
  }

}