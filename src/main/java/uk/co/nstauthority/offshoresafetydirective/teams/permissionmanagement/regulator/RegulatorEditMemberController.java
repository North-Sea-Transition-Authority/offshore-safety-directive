package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasTeamPermission;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRoleUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;

@Controller
@RequestMapping("/permission-management/regulator/{teamId}/edit")
@HasTeamPermission(anyTeamPermissionOf = RolePermission.GRANT_ROLES)
public class RegulatorEditMemberController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.REGULATOR;
  private final TeamMemberService teamMemberService;
  private final TeamMemberViewService teamMemberViewService;
  private final RegulatorTeamMemberEditService regulatorTeamMemberEditService;
  private final RegulatorTeamMemberEditRolesValidator regulatorTeamMemberEditRolesValidator;

  @Autowired
  RegulatorEditMemberController(
      TeamService teamService,
      TeamMemberService teamMemberService,
      TeamMemberViewService teamMemberViewService,
      RegulatorTeamMemberEditService regulatorTeamMemberEditService,
      RegulatorTeamMemberEditRolesValidator regulatorTeamMemberEditRolesValidator) {
    super(teamService);
    this.teamMemberService = teamMemberService;
    this.teamMemberViewService = teamMemberViewService;
    this.regulatorTeamMemberEditService = regulatorTeamMemberEditService;
    this.regulatorTeamMemberEditRolesValidator = regulatorTeamMemberEditRolesValidator;
  }


  @GetMapping("/{wuaId}")
  public ModelAndView renderEditMember(@PathVariable("teamId") TeamId teamId,
                                       @PathVariable("wuaId") WebUserAccountId wuaId) {

    var form = new TeamMemberRolesForm();
    var team = getTeam(teamId, TEAM_TYPE);

    var teamMember = teamMemberService.getTeamMember(team, wuaId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No user [%s] in team [%s]".formatted(wuaId, teamId)));

    var userView = teamMemberViewService.getTeamMemberView(teamMember)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No roles found for user [%s] in team [%s]".formatted(wuaId, teamId)));

    form.setRoles(TeamRoleUtil.getRoleNames(teamMember.roles()));

    return getEditModelAndView(teamId, userView, form);

  }

  @PostMapping("/{wuaId}")
  public ModelAndView editMember(@PathVariable("teamId") TeamId teamId,
                                 @PathVariable("wuaId") WebUserAccountId wuaId,
                                 @ModelAttribute("form") TeamMemberRolesForm form,
                                 BindingResult bindingResult,
                                 @Nullable RedirectAttributes redirectAttributes) {

    var team = getTeam(teamId, TEAM_TYPE);

    var teamMember = teamMemberService.getTeamMember(team, wuaId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No user [%s] in team [%s]".formatted(wuaId, teamId)));

    var userView = teamMemberViewService.getTeamMemberView(teamMember)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No roles found for user [%s] in team [%s]".formatted(wuaId, teamId)));

    regulatorTeamMemberEditRolesValidator.validate(form, bindingResult,
        new RegulatorTeamMemberEditRolesValidatorDto(team, teamMember));

    if (bindingResult.hasErrors()) {
      return getEditModelAndView(teamId, userView, form);
    }

    regulatorTeamMemberEditService.updateRoles(team, teamMember, form.getRoles());

    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Roles updated for %s".formatted(userView.getDisplayName()))
        .build();

    NotificationBannerUtil.applyNotificationBanner(
        Objects.requireNonNull(redirectAttributes),
        notificationBanner
    );

    return ReverseRouter.redirect(on(RegulatorTeamManagementController.class).renderMemberList(teamId));
  }

  private ModelAndView getEditModelAndView(TeamId teamId, TeamMemberView userView, TeamMemberRolesForm form) {
    return new ModelAndView("osd/permissionmanagement/addTeamMemberRolesPage")
        .addObject("form", form)
        .addObject("pageTitle", "What actions does %s perform?".formatted(userView.getDisplayName()))
        .addObject("roles", DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(RegulatorTeamRole.class))
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId))
        );
  }

}
