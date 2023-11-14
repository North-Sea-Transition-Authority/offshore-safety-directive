package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import java.util.Optional;
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
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.EnergyPortalConfiguration;
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
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddTeamMemberForm;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddTeamMemberValidator;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;

@Controller
@RequestMapping("/permission-management/regulator/{teamId}")
@HasTeamPermission(anyTeamPermissionOf = RolePermission.GRANT_ROLES)
class RegulatorAddMemberController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.REGULATOR;

  static final RequestPurpose USER_TO_ADD_PURPOSE = new RequestPurpose("Get user to add to regulator team");

  private final CustomerConfigurationProperties customerConfigurationProperties;

  private final EnergyPortalConfiguration energyPortalConfiguration;

  private final AddTeamMemberValidator addTeamMemberValidator;

  private final EnergyPortalUserService energyPortalUserService;

  private final RegulatorTeamMemberRolesValidator regulatorTeamMemberRolesValidator;

  private final RegulatorTeamService regulatorTeamService;

  @Autowired
  RegulatorAddMemberController(RegulatorTeamService regulatorTeamService,
                               CustomerConfigurationProperties customerConfigurationProperties,
                               EnergyPortalConfiguration energyPortalConfiguration,
                               AddTeamMemberValidator addTeamMemberValidator,
                               EnergyPortalUserService energyPortalUserService,
                               RegulatorTeamMemberRolesValidator regulatorTeamMemberRolesValidator,
                               TeamService teamService) {
    super(teamService);
    this.regulatorTeamService = regulatorTeamService;
    this.customerConfigurationProperties = customerConfigurationProperties;
    this.energyPortalConfiguration = energyPortalConfiguration;
    this.addTeamMemberValidator = addTeamMemberValidator;
    this.energyPortalUserService = energyPortalUserService;
    this.regulatorTeamMemberRolesValidator = regulatorTeamMemberRolesValidator;
  }

  @GetMapping("/add-member")
  ModelAndView renderAddTeamMember(@PathVariable("teamId") TeamId teamId) {
    getTeam(teamId, TEAM_TYPE);
    return getAddTeamMemberModelAndView(teamId, new AddTeamMemberForm());
  }

  @PostMapping("/add-member")
  ModelAndView addMemberToTeamSubmission(@PathVariable("teamId") TeamId teamId,
                                         @ModelAttribute("form") AddTeamMemberForm form,
                                         BindingResult bindingResult) {

    getTeam(teamId, TEAM_TYPE);
    addTeamMemberValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getAddTeamMemberModelAndView(teamId, form);
    }

    var userToAdd = energyPortalUserService.findUserByUsername(form.getUsername(), USER_TO_ADD_PURPOSE).get(0);
    var wuaId = new WebUserAccountId(userToAdd.webUserAccountId());
    if (teamService.isMemberOfTeam(wuaId, teamId)) {
      return ReverseRouter.redirect(on(RegulatorEditMemberController.class).renderEditMember(teamId, wuaId));
    }
    return ReverseRouter.redirect(on(RegulatorAddMemberController.class).renderAddTeamMemberRoles(teamId, wuaId));
  }

  @GetMapping("/add-member/{webUserAccountId}/roles")
  ModelAndView renderAddTeamMemberRoles(@PathVariable("teamId") TeamId teamId,
                                        @PathVariable("webUserAccountId") WebUserAccountId webUserAccountId) {
    getTeam(teamId, TEAM_TYPE);
    var energyPortalUser = getEnergyPortalUser(webUserAccountId);
    return getAddTeamMemberRolesModelAndView(teamId, energyPortalUser, new TeamMemberRolesForm());
  }

  @PostMapping("/add-member/{webUserAccountId}/roles")
  ModelAndView saveAddTeamMemberRoles(@PathVariable("teamId") TeamId teamId,
                                      @PathVariable("webUserAccountId") WebUserAccountId webUserAccountId,
                                      @ModelAttribute("form") TeamMemberRolesForm form,
                                      BindingResult bindingResult,
                                      @Nullable RedirectAttributes redirectAttributes) {
    var team = getTeam(teamId, TEAM_TYPE);
    var energyPortalUser = getEnergyPortalUser(webUserAccountId);

    regulatorTeamMemberRolesValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getAddTeamMemberRolesModelAndView(teamId, energyPortalUser, form);
    }
    var regulatorRoles = form.getRoles()
        .stream()
        .map(RegulatorTeamRole::getRoleFromString)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());

    regulatorTeamService.addUserTeamRoles(team, energyPortalUser, regulatorRoles);

    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Added %s to team".formatted(energyPortalUser.displayName()))
        .build();

    NotificationBannerUtil.applyNotificationBanner(
        Objects.requireNonNull(redirectAttributes),
        notificationBanner
    );

    return ReverseRouter.redirect(on(RegulatorTeamManagementController.class).renderMemberList(teamId));
  }

  private ModelAndView getAddTeamMemberModelAndView(TeamId teamId, AddTeamMemberForm form) {
    return new ModelAndView("osd/permissionmanagement/addTeamMemberPage")
        .addObject("htmlTitle", "Add user to %s".formatted(customerConfigurationProperties.mnemonic()))
        .addObject("registrationUrl", energyPortalConfiguration.registrationUrl())
        .addObject("form", form)
        .addObject(
            "submitUrl",
            ReverseRouter.route(on(RegulatorAddMemberController.class)
                .addMemberToTeamSubmission(teamId, form, ReverseRouter.emptyBindingResult()))
        )
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId))
        );
  }

  private ModelAndView getAddTeamMemberRolesModelAndView(TeamId teamId,
                                                         EnergyPortalUserDto energyPortalUser,
                                                         TeamMemberRolesForm form) {
    return new ModelAndView("osd/permissionmanagement/addTeamMemberRolesPage")
        .addObject("pageTitle", "What actions does %s perform?".formatted(energyPortalUser.displayName()))
        .addObject("form", form)
        .addObject("roles", DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(RegulatorTeamRole.class))
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId))
        );
  }

  private EnergyPortalUserDto getEnergyPortalUser(WebUserAccountId webUserAccountId) {
    var energyPortalUser = energyPortalUserService.findByWuaId(webUserAccountId, USER_TO_ADD_PURPOSE)
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