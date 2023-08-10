package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.streamutil.StreamUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamView;

@Service
public class TeamManagementService {
  private final CustomerConfigurationProperties customerConfigurationProperties;

  @Autowired
  TeamManagementService(CustomerConfigurationProperties customerConfigurationProperties) {
    this.customerConfigurationProperties = customerConfigurationProperties;
  }

  public Map<TeamType, String> getManageTeamTypeUrls(Collection<Team> teams) {
    Set<TeamType> teamTypes = teams.stream()
        .map(Team::getTeamType)
        .collect(Collectors.toSet());

    return teamTypes.stream()
        .sorted(Comparator.comparing(TeamType::getDisplayOrder))
        .collect(StreamUtil.toLinkedHashMap(Function.identity(), this::getRouteForTeamType));
  }

  private String getRouteForTeamType(TeamType teamType) {
    // TODO OSDOP-531 - Change route to the next proxy route when viable.
    return "/";
  }

  public List<TeamView> teamsToTeamViews(List<Team> teams) {
    return teams.stream()
        .map(team -> TeamView.fromTeam(team, customerConfigurationProperties))
        .sorted(TeamView.sort())
        .toList();
  }
}
