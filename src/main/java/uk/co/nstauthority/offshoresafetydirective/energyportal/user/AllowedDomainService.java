package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

import java.util.List;
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
    var group = switch (team.getTeamType()) {
      case TeamType.ORGANISATION_GROUP -> portalOrganisationGroupQueryService
          .findOrganisationById(Integer.parseInt(team.getScopeId()), new RequestPurpose("getOrganisationGroupById"));
      case TeamType.REGULATOR -> portalOrganisationGroupQueryService.getRegulatorOrganisationGroup();
      case TeamType.CONSULTEE -> portalOrganisationGroupQueryService.getConsulteeOrganisationGroup();
    };

    var lowerEmail = userEmail.toLowerCase();
    return group.map(PortalOrganisationGroupDto::getEmailDomains).orElse(List.of()).stream()
        .map(String::toLowerCase)
        .anyMatch(domain -> lowerEmail.endsWith("@" + domain));
  }
}