package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class TeamTestUtil {

  public static TeamView createTeamView(Team team) {
    return new TeamView(new TeamId(team.getUuid()), team.getTeamType(), team.getDisplayName());
  }

  private TeamTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static TeamBuilder Builder() {
    return new TeamBuilder();
  }

  public static class TeamBuilder {

    private UUID uuid = UUID.randomUUID();
    private TeamType teamType = TeamType.REGULATOR;
    private String displayName = "team name";

    public TeamBuilder withId(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public TeamBuilder withTeamType(TeamType teamType) {
      this.teamType = teamType;
      return this;
    }

    public TeamBuilder withDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Team build() {
      var team = new Team(uuid);
      team.setTeamType(teamType);
      team.setDisplayName(displayName);
      return team;
    }
  }
}