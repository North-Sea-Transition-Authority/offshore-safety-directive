package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import java.util.stream.Collectors;
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
import uk.co.nstauthority.offshoresafetydirective.authorisation.RegulatorRolesAllowed;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddTeamMemberForm;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddTeamMemberValidator;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;

@Controller
@RequestMapping("/permission-management/regulator/{teamId}")
@RegulatorRolesAllowed(roles = RegulatorTeamRole.ACCESS_MANAGER)
class RegulatorAddMemberController extends AbstractRegulatorPermissionManagement {

  private final CustomerConfigurationProperties customerConfigurationProperties;

  private final EnergyPortalConfiguration energyPortalConfiguration;

  private final ControllerHelperService controllerHelperService;

  private final AddTeamMemberValidator addTeamMemberValidator;

  private final EnergyPortalUserService energyPortalUserService;

  private final RegulatorTeamMemberRolesValidator regulatorTeamMemberRolesValidator;

  private final RegulatorTeamService regulatorTeamService;

  @Autowired
  RegulatorAddMemberController(RegulatorTeamService regulatorTeamService,
                               CustomerConfigurationProperties customerConfigurationProperties,
                               EnergyPortalConfiguration energyPortalConfiguration,
                               ControllerHelperService controllerHelperService,
                               AddTeamMemberValidator addTeamMemberValidator,
                               EnergyPortalUserService energyPortalUserService,
                               RegulatorTeamMemberRolesValidator regulatorTeamMemberRolesValidator) {
    super(regulatorTeamService);
    this.regulatorTeamService = regulatorTeamService;
    this.customerConfigurationProperties = customerConfigurationProperties;
    this.energyPortalConfiguration = energyPortalConfiguration;
    this.controllerHelperService = controllerHelperService;
    this.addTeamMemberValidator = addTeamMemberValidator;
    this.energyPortalUserService = energyPortalUserService;
    this.regulatorTeamMemberRolesValidator = regulatorTeamMemberRolesValidator;
  }

  @GetMapping("/add-member")
  ModelAndView renderAddTeamMember(@PathVariable("teamId") TeamId teamId) {
    getRegulatorTeam(teamId);
    return getAddTeamMemberModelAndView(teamId, new AddTeamMemberForm());
  }

  @PostMapping("/add-member")
  ModelAndView addMemberToTeamSubmission(@PathVariable("teamId") TeamId teamId,
                                         @ModelAttribute("form") AddTeamMemberForm form,
                                         BindingResult bindingResult) {

    getRegulatorTeam(teamId);
    addTeamMemberValidator.validate(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getAddTeamMemberModelAndView(teamId, form),
        form,
        () -> {
          var userToAdd = energyPortalUserService.findUserByUsername(form.getUsername()).get(0);
          return ReverseRouter.redirect(on(RegulatorAddMemberController.class)
              .renderAddTeamMemberRoles(teamId, new WebUserAccountId(userToAdd.webUserAccountId())));
        }
    );
  }

  @GetMapping("/add-member/{webUserAccountId}/roles")
  ModelAndView renderAddTeamMemberRoles(@PathVariable("teamId") TeamId teamId,
                                        @PathVariable("webUserAccountId") WebUserAccountId webUserAccountId) {
    getRegulatorTeam(teamId);
    var energyPortalUser = getEnergyPortalUser(webUserAccountId);
    return getAddTeamMemberRolesModelAndView(teamId, energyPortalUser, new TeamMemberRolesForm());
  }

  @PostMapping("/add-member/{webUserAccountId}/roles")
  ModelAndView saveAddTeamMemberRoles(@PathVariable("teamId") TeamId teamId,
                                      @PathVariable("webUserAccountId") WebUserAccountId webUserAccountId,
                                      @ModelAttribute("form") TeamMemberRolesForm form,
                                      BindingResult bindingResult) {
    var team = getRegulatorTeam(teamId);
    var energyPortalUser = getEnergyPortalUser(webUserAccountId);

    regulatorTeamMemberRolesValidator.validate(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getAddTeamMemberRolesModelAndView(teamId, energyPortalUser, form),
        form,
        () -> {
          var regulatorRoles = form.getRoles()
              .stream()
              .map(RegulatorTeamRole::getRoleFromString)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .collect(Collectors.toSet());

          regulatorTeamService.addUserTeamRoles(team, energyPortalUser, regulatorRoles);
          return ReverseRouter.redirect(on(RegulatorTeamManagementController.class).renderMemberList(teamId));
        }
    );
  }

  private ModelAndView getAddTeamMemberModelAndView(TeamId teamId, AddTeamMemberForm form) {
    return new ModelAndView("osd/permissionmanagement/regulator/regulatorAddTeamMember")
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
    return new ModelAndView("osd/permissionmanagement/regulator/regulatorAddTeamMemberRoles")
        .addObject("pageTitle", "What actions does %s perform?".formatted(energyPortalUser.displayName()))
        .addObject("form", form)
        .addObject("roles", DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(RegulatorTeamRole.class))
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId))
        );
  }

  private EnergyPortalUserDto getEnergyPortalUser(WebUserAccountId webUserAccountId) {
    var energyPortalUser = energyPortalUserService.findByWuaId(webUserAccountId)
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