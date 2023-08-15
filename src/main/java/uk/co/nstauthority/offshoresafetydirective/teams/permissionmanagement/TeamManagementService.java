package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
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

  public Map<TeamType, String> getManageTeamTypeUrls(Set<TeamType> teamTypes) {
    return teamTypes.stream()
        .sorted(Comparator.comparing(TeamType::getDisplayOrder))
        .collect(StreamUtil.toLinkedHashMap(
            Function.identity(),
            teamType -> ReverseRouter.route(on(TeamSelectionController.class).renderTeamList(teamType.getUrlSlug()))
        ));
  }

  public List<TeamView> teamsToTeamViews(List<Team> teams) {
    return teams.stream()
        .map(team -> TeamView.fromTeam(team, customerConfigurationProperties))
        .sorted(TeamView.sort())
        .toList();
  }
}
