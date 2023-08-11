package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeEditMemberController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeRemoveMemberController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorEditMemberController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorRemoveMemberController;
import uk.co.nstauthority.offshoresafetydirective.userutil.UserDisplayNameUtil;

public record TeamMemberView(WebUserAccountId wuaId, TeamView teamView, String title, String firstName,
                             String lastName, String contactEmail, String contactNumber,
                             Set<TeamRole> teamRoles) {

  public String getDisplayName() {
    return UserDisplayNameUtil.getUserDisplayName(title, firstName, lastName);
  }

  public String removeUrl() {
    return switch (teamView.teamType()) {
      case REGULATOR -> ReverseRouter.route(on(RegulatorRemoveMemberController.class)
          .renderRemoveMember(teamView.teamId(), wuaId));
      case CONSULTEE -> ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
          .renderRemoveMember(teamView.teamId(), wuaId));
      // TODO OSDOP-533 - Remove member from industry team
      case INDUSTRY -> throw new IllegalStateException("Removing member of industry team is not supported");
    };
  }

  public String editUrl() {
    return switch (teamView.teamType()) {
      case REGULATOR -> ReverseRouter.route(on(RegulatorEditMemberController.class)
          .renderEditMember(teamView.teamId(), wuaId));
      case CONSULTEE -> ReverseRouter.route(on(ConsulteeEditMemberController.class)
          .renderEditMember(teamView.teamId(), wuaId));
      // TODO OSDOP-534 - Edit member roles in industry team
      case INDUSTRY -> throw new IllegalStateException("Editing member of industry team is not supported");
    };
  }

}
