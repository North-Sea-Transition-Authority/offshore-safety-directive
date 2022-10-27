package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Service
class RegulatorTeamService {

  private final TeamService teamService;

  private final TeamMemberService teamMemberService;

  private final TeamMemberRoleService teamMemberRoleService;

  @Autowired
  RegulatorTeamService(TeamService teamService, TeamMemberService teamMemberService,
                       TeamMemberRoleService teamMemberRoleService) {
    this.teamService = teamService;
    this.teamMemberService = teamMemberService;
    this.teamMemberRoleService = teamMemberRoleService;
  }

  Optional<Team> getRegulatorTeamForUser(ServiceUserDetail user) {
    return teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.REGULATOR)
        .stream()
        .findFirst();
  }

  Optional<Team> getTeam(TeamId teamId) {
    return teamService.getTeam(teamId, TeamType.REGULATOR);
  }

  boolean isAccessManager(TeamId teamId, ServiceUserDetail user) {
    return teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name()));
  }

  void addUserTeamRoles(Team team, EnergyPortalUserDto userToAdd, Set<RegulatorTeamRole> roles) {
    var rolesAsStrings = roles
        .stream()
        .map(RegulatorTeamRole::name)
        .collect(Collectors.toSet());

    teamMemberRoleService.addUserTeamRoles(team, userToAdd, rolesAsStrings);
  }
}
