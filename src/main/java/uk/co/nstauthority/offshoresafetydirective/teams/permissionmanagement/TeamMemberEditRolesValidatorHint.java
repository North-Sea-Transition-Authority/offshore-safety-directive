package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;

// TODO OSDOP-363 - Make regulator team also use this validator hint
public class TeamMemberEditRolesValidatorHint {

  private Team team;
  private TeamMember teamMember;

  public TeamMemberEditRolesValidatorHint(
      Team team,
      TeamMember teamMember) {
    this.team = team;
    this.teamMember = teamMember;
  }

  public Team getTeam() {
    return team;
  }

  public TeamMember getTeamMember() {
    return teamMember;
  }
}
