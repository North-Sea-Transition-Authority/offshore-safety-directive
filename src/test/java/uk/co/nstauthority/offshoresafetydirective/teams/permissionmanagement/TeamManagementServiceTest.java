package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamView;

@ExtendWith(MockitoExtension.class)
class TeamManagementServiceTest {

  private CustomerConfigurationProperties customerConfigurationProperties;
  private TeamManagementService teamManagementService;

  @BeforeEach
  void setUp() {
    customerConfigurationProperties = new CustomerConfigurationProperties(
        "name", "mnem", "/", "business@email.com"
    );
    teamManagementService = new TeamManagementService(customerConfigurationProperties);
  }

  @Test
  void teamsToTeamViews() {
    var firstTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var secondTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamViews = teamManagementService.teamsToTeamViews(List.of(secondTeam, firstTeam));

    assertThat(teamViews)
        .containsExactly(
            TeamView.fromTeam(firstTeam, customerConfigurationProperties),
            TeamView.fromTeam(secondTeam, customerConfigurationProperties)
        );
  }

  @Test
  void getManageTeamTypeUrls() {
    var regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var firstConsulteeTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var secondConsulteeTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    var result = teamManagementService.getManageTeamTypeUrls(
        List.of(firstConsulteeTeam, regulatorTeam, secondConsulteeTeam)
    );

    assertThat(result)
        .containsExactly(
            entry(
                TeamType.REGULATOR,
                ReverseRouter.route(on(TeamSelectionController.class).renderTeamList(TeamType.REGULATOR.getUrlSlug()))
            ),
            entry(
                TeamType.CONSULTEE,
                ReverseRouter.route(on(TeamSelectionController.class).renderTeamList(TeamType.CONSULTEE.getUrlSlug()))
            )
        );
  }
}