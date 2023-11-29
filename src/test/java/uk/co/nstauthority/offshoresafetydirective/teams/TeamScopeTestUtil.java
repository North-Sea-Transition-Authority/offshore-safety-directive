package uk.co.nstauthority.offshoresafetydirective.teams;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class TeamScopeTestUtil {

  private TeamScopeTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Team team = TeamTestUtil.Builder().build();
    private String portalId = "123";

    private Builder() {
    }

    public Builder withTeam(Team team) {
      this.team = team;
      return this;
    }

    public Builder withPortalId(String portalId) {
      this.portalId = portalId;
      return this;
    }

    public Builder withPortalId(int portalId) {
      this.portalId = String.valueOf(portalId);
      return this;
    }

    public TeamScope build() {
      var teamScope = new TeamScope();
      teamScope.setTeam(team);
      teamScope.setPortalId(portalId);
      return teamScope;
    }

  }

}