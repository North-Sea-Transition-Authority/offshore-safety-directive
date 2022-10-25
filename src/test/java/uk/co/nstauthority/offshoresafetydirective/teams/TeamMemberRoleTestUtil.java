package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class TeamMemberRoleTestUtil {

  private TeamMemberRoleTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder Builder() {
    return new Builder();
  }

  public static class Builder {

    private Builder() {}

    private UUID uuid = UUID.randomUUID();

    private Team team = TeamTestUtil.Builder().build();

    private long webUserAccountId = 100;

    private String role = "TEST_ROLE";

    public Builder withUuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder withTeam(Team team) {
      this.team = team;
      return this;
    }

    public Builder withWebUserAccountId(long webUserAccountId) {
      this.webUserAccountId = webUserAccountId;
      return this;
    }

    public Builder withRole(String role) {
      this.role = role;
      return this;
    }

    public TeamMemberRole build() {
      var teamMemberRole = new TeamMemberRole(uuid);
      teamMemberRole.setTeam(team);
      teamMemberRole.setWuaId(webUserAccountId);
      teamMemberRole.setRole(role);
      return teamMemberRole;
    }

  }
}
