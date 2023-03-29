package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamView;

@Service
public class TeamManagementService {
  private final CustomerConfigurationProperties customerConfigurationProperties;

  @Autowired
  TeamManagementService(CustomerConfigurationProperties customerConfigurationProperties) {
    this.customerConfigurationProperties = customerConfigurationProperties;
  }

  public List<TeamView> teamsToTeamViews(List<Team> teams) {
    return teams.stream()
        .map(team -> TeamView.fromTeam(team, customerConfigurationProperties))
        .sorted(TeamView.sort())
        .toList();
  }
}
