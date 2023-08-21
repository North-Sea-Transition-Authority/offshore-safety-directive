package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRepository;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScope;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Service
class IndustryTeamService {

  private final TeamRepository teamRepository;
  private final TeamScopeService teamScopeService;
  private final TeamMemberRoleService teamMemberRoleService;

  @Autowired
  public IndustryTeamService(TeamRepository teamRepository, TeamScopeService teamScopeService,
                             TeamMemberRoleService teamMemberRoleService) {
    this.teamRepository = teamRepository;
    this.teamScopeService = teamScopeService;
    this.teamMemberRoleService = teamMemberRoleService;
  }

  public Optional<Team> findIndustryTeamForOrganisationGroup(PortalOrganisationGroupDto portalOrganisationGroupDto) {
    return teamScopeService.getTeamScope(
            portalOrganisationGroupDto.organisationGroupId(),
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
        portalOrganisationGroupDto.organisationGroupId()
    );

    return createdTeam;
  }

  void addUserTeamRoles(Team team, EnergyPortalUserDto userToAdd, Set<IndustryTeamRole> roles) {
    var rolesAsStrings = getRolesAsStrings(roles);

    teamMemberRoleService.addUserTeamRoles(team, userToAdd, rolesAsStrings);
  }

  private Set<String> getRolesAsStrings(Set<IndustryTeamRole> industryTeamRoles) {
    return industryTeamRoles.stream()
        .map(IndustryTeamRole::name)
        .collect(Collectors.toSet());
  }
}
