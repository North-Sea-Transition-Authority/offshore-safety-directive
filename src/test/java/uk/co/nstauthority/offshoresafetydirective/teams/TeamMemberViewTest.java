package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryEditMemberController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryRemoveMemberController;
import uk.co.nstauthority.offshoresafetydirective.userutil.UserDisplayNameUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeEditMemberController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeRemoveMemberController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorEditMemberController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorRemoveMemberController;

class TeamMemberViewTest {

  @Test
  void removeUrl_whenRegulatorTeam() {
    var team = TeamTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.REGULATOR)
        .build();
    assertThat(teamMemberView)
        .extracting(TeamMemberView::removeUrl)
        .isEqualTo(ReverseRouter.route(on(RegulatorRemoveMemberController.class).renderRemoveMember(
            team.toTeamId(),
            teamMemberView.wuaId()
        )));
  }

  @Test
  void removeUrl_whenConsulteeTeam() {
    var team = TeamTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.CONSULTEE)
        .build();
    assertThat(teamMemberView)
        .extracting(TeamMemberView::removeUrl)
        .isEqualTo(ReverseRouter.route(on(ConsulteeRemoveMemberController.class).renderRemoveMember(
            team.toTeamId(),
            teamMemberView.wuaId()
        )));
  }

  @Test
  void removeUrl_whenIndustryTeam() {
    var team = TeamTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .build();
    assertThat(teamMemberView)
        .extracting(TeamMemberView::removeUrl)
        .isEqualTo(ReverseRouter.route(on(IndustryRemoveMemberController.class).renderRemoveMember(
            team.toTeamId(),
            teamMemberView.wuaId()
        )));
  }

  @Test
  void editUrl_whenRegulatorTeam() {
    var team = TeamTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.REGULATOR)
        .build();
    assertThat(teamMemberView)
        .extracting(TeamMemberView::editUrl)
        .isEqualTo(ReverseRouter.route(on(RegulatorEditMemberController.class).renderEditMember(
            team.toTeamId(),
            teamMemberView.wuaId()
        )));
  }

  @Test
  void editUrl_whenConsulteeTeam() {
    var team = TeamTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.CONSULTEE)
        .build();
    assertThat(teamMemberView)
        .extracting(TeamMemberView::editUrl)
        .isEqualTo(ReverseRouter.route(on(ConsulteeEditMemberController.class).renderEditMember(
            team.toTeamId(),
            teamMemberView.wuaId()
        )));
  }

  @Test
  void editUrl_whenIndustryTeam() {
    var team = TeamTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .build();
    assertThat(teamMemberView)
        .extracting(TeamMemberView::editUrl)
        .isEqualTo(ReverseRouter.route(on(IndustryEditMemberController.class).renderEditMember(
            team.toTeamId(),
            teamMemberView.wuaId()
        )));
  }

  @Test
  void getDisplayName_whenTitle_thenTitleIncludedInDisplayName() {
    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTitle("Mr")
        .withFirstName("Forename")
        .withLastName("Surname")
        .build();

    assertThat(teamMemberView.getDisplayName())
        .isEqualTo(UserDisplayNameUtil.getUserDisplayName("Mr", "Forename", "Surname"));
  }

  @Test
  void getDisplayName_whenNoTitle_thenTitleNotIncludedInDisplayName() {

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTitle(null)
        .withFirstName("Forename")
        .withLastName("Surname")
        .build();

    assertThat(teamMemberView.getDisplayName())
        .isEqualTo(UserDisplayNameUtil.getUserDisplayName(null, "Forename", "Surname"));
  }
}