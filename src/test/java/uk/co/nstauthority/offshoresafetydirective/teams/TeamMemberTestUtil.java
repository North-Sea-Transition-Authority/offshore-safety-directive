package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

public class TeamMemberTestUtil {

  private TeamMemberTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static TeamMemberBuilder Builder() {
    return new TeamMemberBuilder();
  }

  public static class TeamMemberBuilder {

    private WebUserAccountId webUserAccountId = new WebUserAccountId(123);
    private Set<TeamRole> teamRoles = new HashSet<>();
    private TeamId teamId = new TeamId(UUID.randomUUID());
    private TeamType teamType = TeamType.REGULATOR;

    public TeamMemberBuilder withWebUserAccountId(long webUserAccountId) {
      this.webUserAccountId = new WebUserAccountId(webUserAccountId);
      return this;
    }

    public TeamMemberBuilder withTeamId(TeamId teamId) {
      this.teamId = teamId;
      return this;
    }

    public TeamMemberBuilder withTeamType(TeamType teamType) {
      this.teamType = teamType;
      return this;
    }

    public TeamMemberBuilder withRole(TeamRole teamRole) {
      teamRoles.add(teamRole);
      return this;
    }

    public TeamMemberBuilder withRoles(Set<TeamRole> teamRoles) {
      this.teamRoles = teamRoles;
      return this;
    }

    public TeamMember build() {
      if (teamRoles.isEmpty()) {
        teamRoles.add(RegulatorTeamRole.ACCESS_MANAGER);
      }
      return new TeamMember(webUserAccountId, new TeamView(teamId, teamType), teamRoles);
    }

  }
}
