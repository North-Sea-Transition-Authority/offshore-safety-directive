package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;

public record PortalOrganisationGroupDto(
    String organisationGroupId,
    String name,
    Set<PortalOrganisationDto> organisations
) {

  public static PortalOrganisationGroupDto fromOrganisationGroup(OrganisationGroup organisationGroup) {
    var organisationUnits = Optional.ofNullable(organisationGroup.getOrganisationUnits()).orElse(List.of())
        .stream()
        .map(PortalOrganisationDto::fromOrganisationUnit)
        .collect(Collectors.toSet());

    return new PortalOrganisationGroupDto(
        organisationGroup.getOrganisationGroupId().toString(),
        organisationGroup.getName(),
        organisationUnits
    );
  }
}
