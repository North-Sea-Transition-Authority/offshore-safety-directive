package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRepository;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScope;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Service
class IndustryTeamService {

  private final TeamRepository teamRepository;
  private final TeamScopeService teamScopeService;

  @Autowired
  public IndustryTeamService(TeamRepository teamRepository, TeamScopeService teamScopeService) {
    this.teamRepository = teamRepository;
    this.teamScopeService = teamScopeService;
  }

  public Optional<Team> findIndustryTeamForOrganisationGroup(PortalOrganisationGroupDto portalOrganisationGroupDto) {
    return teamScopeService.getTeamScope(
            portalOrganisationGroupDto.organisationGroupId().toString(),
            PortalTeamType.ORGANISATION_GROUP)
        .map(TeamScope::getTeam);
  }

  @Transactional
  public Team createIndustryTeam(PortalOrganisationGroupDto portalOrganisationGroupDto) {
    var team = new Team();
    team.setTeamType(TeamType.INDUSTRY);
    team.setDisplayName(portalOrganisationGroupDto.name());
    var createdTeam = teamRepository.save(team);

    teamScopeService.addTeamScope(
        createdTeam,
        PortalTeamType.ORGANISATION_GROUP,
        portalOrganisationGroupDto.organisationGroupId().toString()
    );

    return createdTeam;
  }
}
