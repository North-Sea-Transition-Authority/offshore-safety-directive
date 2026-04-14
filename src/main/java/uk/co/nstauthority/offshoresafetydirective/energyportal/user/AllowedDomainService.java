package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Service
public class AllowedDomainService {

  private final PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  AllowedDomainService(PortalOrganisationGroupQueryService portalOrganisationGroupQueryService) {
    this.portalOrganisationGroupQueryService = portalOrganisationGroupQueryService;
  }

  public boolean isAllowedDomain(String userEmail, Team team) {
    Optional<PortalOrganisationGroupDto> group;
    switch (team.getTeamType()) {
      case TeamType.ORGANISATION_GROUP -> group = portalOrganisationGroupQueryService
          .findOrganisationById(Integer.parseInt(team.getScopeId()), new RequestPurpose("getOrganisationGroupById"));
      case TeamType.REGULATOR -> group = portalOrganisationGroupQueryService.getRegulatorOrganisationGroup();
      case TeamType.CONSULTEE ->  group = portalOrganisationGroupQueryService.getConsulteeOrganisationGroup();
      default -> throw new IllegalStateException("Unexpected value: " + team.getTeamType());
    }

    List<String> emailDomains = List.of();
    if (group.isPresent()) {
      emailDomains = group.get().getEmailDomains();
    }

    return emailDomains.contains(userEmail.split("@")[1]);
  }
}