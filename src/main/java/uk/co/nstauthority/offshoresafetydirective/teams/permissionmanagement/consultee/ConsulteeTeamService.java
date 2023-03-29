package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee;

import java.util.List;
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
public class ConsulteeTeamService {

  private final TeamService teamService;
  private final TeamMemberService teamMemberService;
  private final TeamMemberRoleService teamMemberRoleService;

  @Autowired
  ConsulteeTeamService(TeamService teamService,
                       TeamMemberService teamMemberService,
                       TeamMemberRoleService teamMemberRoleService) {
    this.teamService = teamService;
    this.teamMemberService = teamMemberService;
    this.teamMemberRoleService = teamMemberRoleService;
  }

  public boolean isAccessManager(TeamId teamId, ServiceUserDetail user) {
    return teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(ConsulteeTeamRole.ACCESS_MANAGER.name()));
  }

  Optional<Team> getTeam(TeamId teamId) {
    return teamService.getTeam(teamId, TeamType.CONSULTEE);
  }

  List<Team> getTeamsForUser(ServiceUserDetail userDetail) {
    return teamService.getTeamsOfTypeThatUserBelongsTo(userDetail, TeamType.CONSULTEE);
  }

  void addUserTeamRoles(Team team, EnergyPortalUserDto userToAdd, Set<ConsulteeTeamRole> roles) {
    var rolesAsStrings = getRolesAsStrings(roles);

    teamMemberRoleService.addUserTeamRoles(team, userToAdd, rolesAsStrings);
  }

  private Set<String> getRolesAsStrings(Set<ConsulteeTeamRole> consulteeTeamRoles) {
    return consulteeTeamRoles.stream()
        .map(ConsulteeTeamRole::name)
        .collect(Collectors.toSet());
  }
}
