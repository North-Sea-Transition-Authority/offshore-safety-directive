package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

@Service
public class TeamMemberRemovalService {

  public static final String LAST_ACCESS_MANAGER_ERROR_MESSAGE = "You cannot remove the last access manager of a team";

  private final TeamMemberService teamMemberService;

  private final TeamMemberRoleService teamMemberRoleService;

  @Autowired
  public TeamMemberRemovalService(TeamMemberService teamMemberService, TeamMemberRoleService teamMemberRoleService) {
    this.teamMemberService = teamMemberService;
    this.teamMemberRoleService = teamMemberRoleService;
  }

  public void removeTeamMember(Team team, TeamMember teamMember, TeamRole accessManagerTeamRole) {
    if (canRemoveTeamMember(team, teamMember.wuaId(), accessManagerTeamRole)) {
      teamMemberRoleService.removeMemberFromTeam(team, teamMember);
    } else {
      throw new IllegalStateException(
          "User [%s] cannot be removed from team [%s] as they are the last access manager"
              .formatted(teamMember.wuaId(), team.getUuid())
      );
    }
  }

  public boolean canRemoveTeamMember(Team team, WebUserAccountId memberWuaId, TeamRole accessManagerTeamRole) {
    var teamMembers = teamMemberService.getTeamMembers(team);
    var accessManagers = filterAccessManagers(teamMembers, accessManagerTeamRole);
    return accessManagers.stream()
        .anyMatch(tm -> !tm.wuaId().equals(memberWuaId));
  }

  private List<TeamMember> filterAccessManagers(Collection<TeamMember> teamMembers, TeamRole accessManagerTeamRole) {
    return teamMembers.stream()
        .filter(teamMember -> teamMember.roles().contains(accessManagerTeamRole))
        .toList();
  }

}
