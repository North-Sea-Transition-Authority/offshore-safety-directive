package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public class TeamMemberViewUtil {

  private TeamMemberViewUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static TeamMemberView getTeamMemberView(Set<TeamRole> teamRoles) {
    return new TeamMemberView(1, "Mr", "Forename", "M.I.", "Surname",
        "f.s@test.com", "+440000000000", teamRoles);
  }

}
