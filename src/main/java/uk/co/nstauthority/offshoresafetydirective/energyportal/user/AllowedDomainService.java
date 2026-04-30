package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

import java.util.ArrayList;
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

    var emailDomains = group.map(PortalOrganisationGroupDto::getEmailDomains).orElseGet(ArrayList::new);
    return emailDomains.stream()
        .map(String::toLowerCase)
        .anyMatch(domain -> userEmail.toLowerCase().endsWith('@' + domain));
  }
}