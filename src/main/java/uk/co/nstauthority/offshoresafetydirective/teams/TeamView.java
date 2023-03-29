package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamManagementController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamManagementController;

public record TeamView(TeamId teamId, TeamType teamType, String displayName) {

  public String teamUrl() {
    return switch (teamType) {
      case REGULATOR -> ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId));
      case CONSULTEE -> ReverseRouter.route(on(ConsulteeTeamManagementController.class).renderMemberList(teamId));
    };
  }

  public static TeamView fromTeam(Team team, CustomerConfigurationProperties customerConfigurationProperties) {
    var teamId = new TeamId(team.getUuid());
    var teamName = switch (team.getTeamType()) {
      case REGULATOR -> customerConfigurationProperties.mnemonic();
      case CONSULTEE -> team.getDisplayName();
    };
    return new TeamView(teamId, team.getTeamType(), teamName);
  }

  public static TeamViewComparator sort() {
    return new TeamViewComparator();
  }

}
