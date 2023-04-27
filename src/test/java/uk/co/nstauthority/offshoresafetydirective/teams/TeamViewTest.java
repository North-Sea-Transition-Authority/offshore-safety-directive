package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamManagementController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamManagementController;

class TeamViewTest {

  private CustomerConfigurationProperties customerConfigurationProperties;

  @BeforeEach
  void setUp() {
    this.customerConfigurationProperties = new CustomerConfigurationProperties(
        "stub",
        "mnem",
        "/",
        "email@wios.co.uk"
    );
  }

  @Test
  void teamUrl_whenRegulator() {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var teamView = TeamView.fromTeam(team, customerConfigurationProperties);
    assertThat(teamView)
        .extracting(
            TeamView::teamUrl,
            TeamView::displayName
        )
        .containsExactly(
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(team.toTeamId())),
            customerConfigurationProperties.mnemonic()
        );
  }

  @Test
  void teamUrl_whenConsultee() {
    var teamName = "test team";
    var team = TeamTestUtil.Builder()
        .withDisplayName(teamName)
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamView = TeamView.fromTeam(team, customerConfigurationProperties);
    assertThat(teamView)
        .extracting(
            TeamView::teamUrl,
            TeamView::displayName
        )
        .containsExactly(
            ReverseRouter.route(on(ConsulteeTeamManagementController.class).renderMemberList(team.toTeamId())),
            teamName
        );
  }

  @Test
  void fromTeam_whenRegulator() {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var teamView = TeamView.fromTeam(team, customerConfigurationProperties);
    assertThat(teamView)
        .extracting(TeamView::displayName)
        .isEqualTo(customerConfigurationProperties.mnemonic())
        .isNotEqualTo(team.getDisplayName());
  }

  @Test
  void fromTeam_whenConsultee() {
    var teamName = "fromTeam consultee team";
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withDisplayName(teamName)
        .build();
    var teamView = TeamView.fromTeam(team, customerConfigurationProperties);
    assertThat(teamView)
        .extracting(TeamView::displayName)
        .isEqualTo(teamName)
        .isNotEqualTo(customerConfigurationProperties.mnemonic());
  }
}