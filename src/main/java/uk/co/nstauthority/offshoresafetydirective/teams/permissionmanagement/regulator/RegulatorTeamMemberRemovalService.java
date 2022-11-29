package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;

@Service
class RegulatorTeamMemberRemovalService {

  public static final String LAST_ACCESS_MANAGER_ERROR_MESSAGE = "You cannot remove the last access manager of a team";

  private final TeamMemberService teamMemberService;

  private final TeamMemberRoleService teamMemberRoleService;

  @Autowired
  RegulatorTeamMemberRemovalService(TeamMemberService teamMemberService, TeamMemberRoleService teamMemberRoleService) {
    this.teamMemberService = teamMemberService;
    this.teamMemberRoleService = teamMemberRoleService;
  }

  void removeTeamMember(Team team, TeamMember teamMember) {
    if (canRemoveTeamMember(team, teamMember)) {
      teamMemberRoleService.removeMemberFromTeam(team, teamMember);
    } else {
      throw new IllegalStateException(
          "User [%s] cannot be removed from team [%s] as they are the last access manager"
              .formatted(teamMember.wuaId(), team.getUuid())
      );
    }
  }

  boolean canRemoveTeamMember(Team team, TeamMember teamMember) {
    var teamMembers = teamMemberService.getTeamMembers(team);
    var accessManagers = filterAccessManagers(teamMembers);
    return accessManagers.stream().anyMatch(
        tm -> !tm.wuaId().equals(teamMember.wuaId()));
  }

  private List<TeamMember> filterAccessManagers(Collection<TeamMember> teamMembers) {
    return teamMembers.stream()
        .filter(teamMember ->
            teamMember.roles().stream().anyMatch(teamRole -> teamRole.equals(RegulatorTeamRole.ACCESS_MANAGER)))
        .toList();
  }

}
