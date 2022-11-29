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
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;

@Controller
@RequestMapping("/permission-management/regulator/{teamId}/remove")
@RegulatorRolesAllowed(roles = {RegulatorTeamRole.ACCESS_MANAGER})
public class RegulatorRemoveMemberController extends AbstractRegulatorPermissionManagement {

  private final TeamMemberService teamMemberService;
  private final CustomerConfigurationProperties customerConfigurationProperties;
  private final TeamMemberViewService teamMemberViewService;
  private final RegulatorTeamMemberRemovalService regulatorTeamMemberRemovalService;

  @Autowired
  public RegulatorRemoveMemberController(
      RegulatorTeamService regulatorTeamService,
      TeamMemberService teamMemberService,
      CustomerConfigurationProperties customerConfigurationProperties,
      TeamMemberViewService teamMemberViewService,
      RegulatorTeamMemberRemovalService regulatorTeamMemberRemovalService) {
    super(regulatorTeamService);
    this.teamMemberService = teamMemberService;
    this.customerConfigurationProperties = customerConfigurationProperties;
    this.teamMemberViewService = teamMemberViewService;
    this.regulatorTeamMemberRemovalService = regulatorTeamMemberRemovalService;
  }

  @GetMapping("/{wuaId}")
  public ModelAndView renderRemoveMember(@PathVariable("teamId") TeamId teamId,
                                         @PathVariable("wuaId") WebUserAccountId wuaId) {

    var team = getRegulatorTeam(teamId);

    var teamMemberOptional = teamMemberService.getTeamMember(team, wuaId);

    if (teamMemberOptional.isEmpty()) {
      return ReverseRouter.redirect(on(RegulatorTeamManagementController.class).renderMemberList(teamId));
    }

    var teamMember = teamMemberOptional.get();

    var teamMemberView = teamMemberViewService.getTeamMemberView(teamMember)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No roles found for user [%s] in team [%s]".formatted(wuaId, teamId)));

    var teamName = customerConfigurationProperties.mnemonic();

    var canRemoveTeamMember = regulatorTeamMemberRemovalService.canRemoveTeamMember(team, teamMember);

    return new ModelAndView("osd/permissionmanagement/regulator/regulatorRemoveTeamMember")
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

    var team = getRegulatorTeam(teamId);

    var teamMember = teamMemberService.getTeamMember(team, wuaId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No user [%s] in team [%s]".formatted(wuaId, teamId)));

    if (regulatorTeamMemberRemovalService.canRemoveTeamMember(team, teamMember)) {
      regulatorTeamMemberRemovalService.removeTeamMember(team, teamMember);
    } else {
      return renderRemoveMember(teamId, wuaId)
          .addObject("singleErrorMessage", RegulatorTeamMemberRemovalService.LAST_ACCESS_MANAGER_ERROR_MESSAGE);
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
