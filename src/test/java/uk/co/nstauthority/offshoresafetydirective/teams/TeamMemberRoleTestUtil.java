package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class TeamMemberRoleTestUtil {

  private TeamMemberRoleTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder Builder() {
    return new Builder();
  }

  static class Builder {

    private Builder() {}

    private UUID uuid = UUID.randomUUID();

    private Team team = TeamTestUtil.Builder().build();

    private long webUserAccountId = 100;

    private String role = "TEST_ROLE";

    Builder withUuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    Builder withTeam(Team team) {
      this.team = team;
      return this;
    }

    Builder withWebUserAccountId(long webUserAccountId) {
      this.webUserAccountId = webUserAccountId;
      return this;
    }

    Builder withRole(String role) {
      this.role = role;
      return this;
    }

    TeamMemberRole build() {
      var teamMemberRole = new TeamMemberRole(uuid);
      teamMemberRole.setTeam(team);
      teamMemberRole.setWuaId(webUserAccountId);
      teamMemberRole.setRole(role);
      return teamMemberRole;
    }

  }
}
