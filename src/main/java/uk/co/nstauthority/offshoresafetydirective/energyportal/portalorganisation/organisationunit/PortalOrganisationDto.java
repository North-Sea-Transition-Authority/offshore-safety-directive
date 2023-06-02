package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.Objects;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;

public record PortalOrganisationDto(Integer id,
                                    String name,
                                    OrganisationRegisteredNumber registeredNumber,
                                    boolean isActive,
                                    boolean isDuplicate) {

  static PortalOrganisationDto fromOrganisationUnit(OrganisationUnit organisationUnit) {
    return new PortalOrganisationDto(
        organisationUnit.getOrganisationUnitId(),
        organisationUnit.getName(),
        new OrganisationRegisteredNumber(organisationUnit.getRegisteredNumber()),
        // OrganisationUnit from Energy Portal API provides Boolean type so to avoid
        // dealing with a possible null, assume active unless explicitly told by EPA
        Objects.requireNonNullElse(organisationUnit.getIsActive(), true),
        Objects.requireNonNullElse(organisationUnit.getIsDuplicate(), false)
    );
  }

  public String displayName() {
    return OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(this);
  }

}