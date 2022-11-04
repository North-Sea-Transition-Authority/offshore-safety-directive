package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;

@Service
class RegulatorTeamMemberRemovalService extends TeamMemberPersistenceService {

  public static final String LAST_ACCESS_MANAGER_ERROR_MESSAGE = "You cannot remove the last access manager of a team";

  private final TeamMemberService teamMemberService;

  @Autowired
  RegulatorTeamMemberRemovalService(TeamMemberService teamMemberService) {
    this.teamMemberService = teamMemberService;
  }

  void removeTeamMember(Team team, TeamMember teamMember) {
    if (canRemoveTeamMember(team, teamMember)) {
      super.removeMemberFromTeam(team, teamMember);
    } else {
      throw new IllegalStateException(
          "User [%s] cannot be removed from team [%s] as they are the last access manager".formatted(
              teamMember.wuaId(), team.getUuid()));
    }
  }

  public String getRemoveScreenPageTitle(String teamName, TeamMemberView teamMemberView, boolean canRemoveTeamMember) {
    if (canRemoveTeamMember) {
      return getAskToRemovePageTitleText(teamName, teamMemberView);
    }
    return getUnableToRemovePageTitleText(teamName, teamMemberView);
  }

  String getAskToRemovePageTitleText(String teamName, TeamMemberView teamMemberView) {
    return "Are you sure you want to remove %s from %s?".formatted(teamMemberView.getDisplayName(), teamName);
  }

  String getUnableToRemovePageTitleText(String teamName, TeamMemberView teamMemberView) {
    return "You are unable to remove %s from %s".formatted(teamMemberView.getDisplayName(), teamName);
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
