package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasTeamPermission;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;

@Controller
@RequestMapping("/permission-management/consultee/{teamId}")
@HasTeamPermission(
    anyTeamPermissionOf = RolePermission.GRANT_ROLES,
    anyNonTeamPermissionOf = RolePermission.MANAGE_CONSULTEE_TEAMS
)
class ConsulteeAddRolesController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.CONSULTEE;
  static final RequestPurpose ROLES_TO_ADD_PURPOSE = new RequestPurpose("Get user to add roles to");

  private final ControllerHelperService controllerHelperService;
  private final EnergyPortalUserService energyPortalUserService;
  private final ConsulteeTeamMemberRolesValidator consulteeTeamMemberRolesValidator;
  private final ConsulteeTeamService consulteeTeamService;

  @Autowired
  protected ConsulteeAddRolesController(
      ControllerHelperService controllerHelperService,
      EnergyPortalUserService energyPortalUserService,
      ConsulteeTeamMemberRolesValidator consulteeTeamMemberRolesValidator,
      ConsulteeTeamService consulteeTeamService,
      TeamService teamService) {
    super(teamService);
    this.controllerHelperService = controllerHelperService;
    this.energyPortalUserService = energyPortalUserService;
    this.consulteeTeamMemberRolesValidator = consulteeTeamMemberRolesValidator;
    this.consulteeTeamService = consulteeTeamService;
  }

  @GetMapping("/add-member/{wuaId}/roles")
  public ModelAndView renderAddTeamMemberRoles(@PathVariable("teamId") TeamId teamId,
                                               @PathVariable("wuaId") WebUserAccountId webUserAccountId) {
    getTeam(teamId, TEAM_TYPE);
    var energyPortalUser = getEnergyPortalUser(webUserAccountId);
    return getAddTeamMemberRolesModelAndView(teamId, energyPortalUser, new TeamMemberRolesForm())
        .addObject("roles", DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(ConsulteeTeamRole.class))
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ConsulteeAddMemberController.class).renderAddTeamMember(teamId)));
  }

  @PostMapping("/add-member/{wuaId}/roles")
  protected ModelAndView saveAddTeamMemberRoles(@PathVariable("teamId") TeamId teamId,
                                                @PathVariable("wuaId") WebUserAccountId webUserAccountId,
                                                @ModelAttribute("form") TeamMemberRolesForm form,
                                                BindingResult bindingResult,
                                                @Nullable RedirectAttributes redirectAttributes) {
    var team = getTeam(teamId, TEAM_TYPE);
    var energyPortalUser = getEnergyPortalUser(webUserAccountId);
    consulteeTeamMemberRolesValidator.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getAddTeamMemberRolesModelAndView(teamId, energyPortalUser, form),
        form,
        () -> {
          var regulatorRoles = getRolesToAdd(form.getRoles());
          consulteeTeamService.addUserTeamRoles(team, energyPortalUser, regulatorRoles);

          var notificationBanner = NotificationBanner.builder()
              .withBannerType(NotificationBannerType.SUCCESS)
              .withHeading("Added %s to team".formatted(energyPortalUser.displayName()))
              .build();

          NotificationBannerUtil.applyNotificationBanner(
              Objects.requireNonNull(redirectAttributes),
              notificationBanner
          );

          return ReverseRouter.redirect(on(ConsulteeTeamManagementController.class).renderMemberList(teamId));
        }
    );
  }

  private Set<ConsulteeTeamRole> getRolesToAdd(Set<String> rolesToAdd) {
    return rolesToAdd.stream()
        .map(ConsulteeTeamRole::getRoleFromString)
        .flatMap(Optional::stream)
        .collect(Collectors.toSet());
  }

  private ModelAndView getAddTeamMemberRolesModelAndView(TeamId teamId,
                                                         EnergyPortalUserDto energyPortalUser,
                                                         TeamMemberRolesForm form) {
    return new ModelAndView("osd/permissionmanagement/addTeamMemberRolesPage")
        .addObject("pageTitle", "What actions does %s perform?".formatted(energyPortalUser.displayName()))
        .addObject("form", form)
        .addObject("roles", DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(ConsulteeTeamRole.class))
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(ConsulteeAddMemberController.class).renderAddTeamMember(teamId))
        );
  }

  // TODO OSDOP-426 - Merge with RegulatorAddRolesController usage
  private EnergyPortalUserDto getEnergyPortalUser(WebUserAccountId webUserAccountId) {
    var energyPortalUser = energyPortalUserService.findByWuaId(webUserAccountId, ROLES_TO_ADD_PURPOSE)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No Energy Portal user with WUA_ID %s could be found".formatted(webUserAccountId)
        ));

    if (energyPortalUser.isSharedAccount()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Energy Portal user with WUA_ID %s is a shared account and is not allowed to be added to this service"
              .formatted(webUserAccountId)
      );
    }

    if (!energyPortalUser.canLogin()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          """
                  Energy Portal user with WUA_ID %s does not have login access to the Energy Portal and is
                  not allowed to be added to this service
              """
              .formatted(webUserAccountId)
      );
    }

    return energyPortalUser;
  }
}