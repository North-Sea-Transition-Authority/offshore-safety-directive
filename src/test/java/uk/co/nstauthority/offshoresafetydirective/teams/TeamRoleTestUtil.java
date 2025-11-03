package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.UUID;

public class TeamRoleTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private Team team = TeamTestUtil.newBuilder().build();
    private Role role = Role.TEAM_MANAGER;
    private Long wuaId = 1L;

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withTeam(Team team) {
      this.team = team;
      return this;
    }

    public Builder withRole(Role role) {
      this.role = role;
      return this;
    }

    public Builder withWuaId(Long wuaId) {
      this.wuaId = wuaId;
      return this;
    }

    public TeamRole build() {
      var teamRole = new TeamRole(id);
      teamRole.setTeam(team);
      teamRole.setRole(role);
      teamRole.setWuaId(wuaId);

      return teamRole;
    }

    private Builder() {
    }

  }

}
