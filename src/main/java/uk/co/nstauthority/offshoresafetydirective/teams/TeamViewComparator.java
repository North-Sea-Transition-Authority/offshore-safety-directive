package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Comparator;
import java.util.function.Function;

class TeamViewComparator implements Comparator<TeamView> {

  @Override
  public int compare(TeamView teamView1, TeamView teamView2) {
    return Comparator
        .comparing(byTypeDisplayOrder())
        .thenComparing(byTeamName())
        .compare(teamView1, teamView2);
  }

  private Function<TeamView, Integer> byTypeDisplayOrder() {
    return teamView -> teamView.teamType().getDisplayOrder();
  }

  private Function<TeamView, String> byTeamName() {
    return teamView -> teamView.displayName().toLowerCase();
  }
}
