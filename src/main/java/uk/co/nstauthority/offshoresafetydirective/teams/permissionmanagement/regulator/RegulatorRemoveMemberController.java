package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasTeamPermission;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRemovalService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/permission-management/regulator/{teamId}/remove")
@HasTeamPermission(anyTeamPermissionOf = RolePermission.GRANT_ROLES)
public class RegulatorRemoveMemberController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.REGULATOR;

  private final TeamMemberService teamMemberService;
  private final CustomerConfigurationProperties customerConfigurationProperties;
  private final TeamMemberViewService teamMemberViewService;
  private final TeamMemberRemovalService teamMemberRemovalService;

  @Autowired
  public RegulatorRemoveMemberController(
      TeamService teamService,
      TeamMemberService teamMemberService,
      CustomerConfigurationProperties customerConfigurationProperties,
      TeamMemberViewService teamMemberViewService,
      TeamMemberRemovalService teamMemberRemovalService) {
    super(teamService);
    this.teamMemberService = teamMemberService;
    this.customerConfigurationProperties = customerConfigurationProperties;
    this.teamMemberViewService = teamMemberViewService;
    this.teamMemberRemovalService = teamMemberRemovalService;
  }

  @GetMapping("/{wuaId}")
  public ModelAndView renderRemoveMember(@PathVariable("teamId") TeamId teamId,
                                         @PathVariable("wuaId") WebUserAccountId wuaId) {

    var team = getTeam(teamId, TEAM_TYPE);

    var teamMemberOptional = teamMemberService.getTeamMember(team, wuaId);

    if (teamMemberOptional.isEmpty()) {
      return ReverseRouter.redirect(on(RegulatorTeamManagementController.class).renderMemberList(teamId));
    }

    var teamMember = teamMemberOptional.get();

    var teamMemberView = teamMemberViewService.getTeamMemberView(teamMember)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No roles found for user [%s] in team [%s]".formatted(wuaId, teamId)));

    var teamName = customerConfigurationProperties.mnemonic();

    var canRemoveTeamMember = teamMemberRemovalService.canRemoveTeamMember(team, wuaId,
        RegulatorTeamRole.ACCESS_MANAGER);

    return new ModelAndView("osd/permissionmanagement/removeTeamMemberPage")
        .addObject("pageTitle", getPageTitle(canRemoveTeamMember, teamMemberView, teamName))
        .addObject("teamName", teamName)
        .addObject("teamMember", teamMemberView)
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId))
        )
        .addObject(
            "removeUrl",
            ReverseRouter.route(on(RegulatorRemoveMemberController.class).removeMember(teamId, wuaId, null))
        )
        .addObject("canRemoveTeamMember", canRemoveTeamMember);
  }

  @PostMapping("/{wuaId}")
  public ModelAndView removeMember(@PathVariable("teamId") TeamId teamId,
                                   @PathVariable("wuaId") WebUserAccountId wuaId,
                                   RedirectAttributes redirectAttributes) {

    var team = getTeam(teamId, TEAM_TYPE);

    var teamMember = teamMemberService.getTeamMember(team, wuaId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No user [%s] in team [%s]".formatted(wuaId, teamId)));

    var accessManagerRole = RegulatorTeamRole.ACCESS_MANAGER;

    if (teamMemberRemovalService.canRemoveTeamMember(team, wuaId, accessManagerRole)) {
      teamMemberRemovalService.removeTeamMember(team, teamMember, accessManagerRole);
    } else {
      return renderRemoveMember(teamId, wuaId)
          .addObject("singleErrorMessage", TeamMemberRemovalService.LAST_ACCESS_MANAGER_ERROR_MESSAGE);
    }

    var userView = teamMemberViewService.getTeamMemberView(teamMember)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
            "No roles found for user [%s] in team [%s]".formatted(wuaId, teamId)));

    var banner = NotificationBanner.builder()
        .withTitle("Removed member from team")
        .withBannerType(NotificationBannerType.SUCCESS)
        .withContent("%s has been removed from the team".formatted(userView.getDisplayName()))
        .build();

    NotificationBannerUtil.applyNotificationBanner(redirectAttributes, banner);

    return ReverseRouter.redirect(on(RegulatorTeamManagementController.class).renderMemberList(teamId));

  }

  private String getPageTitle(boolean canRemoveMember, TeamMemberView teamMemberView, String teamName) {
    return canRemoveMember
        ? "Are you sure you want to remove %s from %s?".formatted(teamMemberView.getDisplayName(), teamName)
        : "You are unable to remove %s from %s".formatted(teamMemberView.getDisplayName(), teamName);
  }
}
