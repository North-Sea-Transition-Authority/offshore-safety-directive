package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRoleUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;

@Controller
@RequestMapping("/permission-management/regulator/{teamId}/edit")
@RegulatorRolesAllowed(roles = {RegulatorTeamRole.ACCESS_MANAGER})
public class RegulatorEditMemberController extends AbstractRegulatorPermissionManagement {

  private final RegulatorTeamService regulatorTeamService;
  private final TeamMemberService teamMemberService;
  private final TeamMemberViewService teamMemberViewService;
  private final RegulatorTeamMemberEditService regulatorTeamMemberEditService;
  private final ControllerHelperService controllerHelperService;
  private final RegulatorTeamMemberEditRolesValidator regulatorTeamMemberEditRolesValidator;

  @Autowired
  RegulatorEditMemberController(
      RegulatorTeamService regulatorTeamService,
      TeamMemberService teamMemberService,
      TeamMemberViewService teamMemberViewService,
      RegulatorTeamMemberEditService regulatorTeamMemberEditService,
      ControllerHelperService controllerHelperService,
      RegulatorTeamMemberEditRolesValidator regulatorTeamMemberEditRolesValidator) {
    super(regulatorTeamService);
    this.regulatorTeamService = regulatorTeamService;
    this.teamMemberService = teamMemberService;
    this.teamMemberViewService = teamMemberViewService;
    this.regulatorTeamMemberEditService = regulatorTeamMemberEditService;
    this.controllerHelperService = controllerHelperService;
    this.regulatorTeamMemberEditRolesValidator = regulatorTeamMemberEditRolesValidator;
  }


  @GetMapping("/{wuaId}")
  public ModelAndView renderEditMember(@PathVariable("teamId") TeamId teamId,
                                       @PathVariable("wuaId") WebUserAccountId wuaId) {

    var form = new TeamMemberRolesForm();
    var team = getRegulatorTeam(teamId);

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
                                 BindingResult bindingResult) {

    var team = getRegulatorTeam(teamId);

    var teamMember = teamMemberService.getTeamMember(team, wuaId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No user [%s] in team [%s]".formatted(wuaId, teamId)));

    var userView = teamMemberViewService.getTeamMemberView(teamMember)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No roles found for user [%s] in team [%s]".formatted(wuaId, teamId)));

    regulatorTeamMemberEditRolesValidator.validate(form, bindingResult,
        new RegulatorTeamMemberEditRolesValidatorDto(team, teamMember));

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getEditModelAndView(teamId, userView, form),
        form,
        () -> {
          regulatorTeamMemberEditService.updateRoles(team, teamMember, form.getRoles());
          return ReverseRouter.redirect(on(RegulatorTeamManagementController.class).renderMemberList(teamId));
        });

  }

  private ModelAndView getEditModelAndView(TeamId teamId, TeamMemberView userView, TeamMemberRolesForm form) {
    return new ModelAndView("osd/permissionmanagement/regulator/regulatorAddTeamMemberRoles")
        .addObject("form", form)
        .addObject("pageTitle", "What actions does %s perform?".formatted(userView.getDisplayName()))
        .addObject("roles", DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(RegulatorTeamRole.class))
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId))
        );
  }

}
