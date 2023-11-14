package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasTeamPermission;
import uk.co.nstauthority.offshoresafetydirective.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddTeamMemberForm;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddTeamMemberValidator;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/permission-management/industry/{teamId}")
@HasTeamPermission(
    anyTeamPermissionOf = RolePermission.GRANT_ROLES,
    anyNonTeamPermissionOf = RolePermission.MANAGE_INDUSTRY_TEAMS
)
public class IndustryAddMemberController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.INDUSTRY;

  static final RequestPurpose USER_TO_ADD_PURPOSE = new RequestPurpose("Get user to add to industry team");

  private final AddTeamMemberValidator addTeamMemberValidator;
  private final EnergyPortalUserService energyPortalUserService;
  private final EnergyPortalConfiguration energyPortalConfiguration;

  @Autowired
  IndustryAddMemberController(AddTeamMemberValidator addTeamMemberValidator,
                              EnergyPortalUserService energyPortalUserService,
                              EnergyPortalConfiguration energyPortalConfiguration,
                              TeamService teamService) {
    super(teamService);
    this.addTeamMemberValidator = addTeamMemberValidator;
    this.energyPortalUserService = energyPortalUserService;
    this.energyPortalConfiguration = energyPortalConfiguration;
  }

  @GetMapping("/add-member")
  public ModelAndView renderAddTeamMember(@PathVariable("teamId") TeamId teamId) {
    var team = getTeam(teamId, TEAM_TYPE);
    var form = new AddTeamMemberForm();
    return getAddTeamMemberModelAndView(form, team);
  }

  @PostMapping("/add-member")
  public ModelAndView addMemberToTeamSubmission(@PathVariable("teamId") TeamId teamId,
                                                @ModelAttribute("form") AddTeamMemberForm form,
                                                BindingResult bindingResult) {
    var team = getTeam(teamId, TEAM_TYPE);
    addTeamMemberValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getAddTeamMemberModelAndView(form, team);
    }

    var userToAdd = energyPortalUserService.findUserByUsername(form.getUsername(), USER_TO_ADD_PURPOSE).get(0);
    var wuaId = new WebUserAccountId(userToAdd.webUserAccountId());
    if (teamService.isMemberOfTeam(wuaId, teamId)) {
      return ReverseRouter.redirect(on(IndustryEditMemberController.class).renderEditMember(teamId, wuaId));
    }
    return ReverseRouter.redirect(on(IndustryAddRolesController.class).renderAddTeamMemberRoles(teamId, wuaId));
  }

  private ModelAndView getAddTeamMemberModelAndView(AddTeamMemberForm form, Team team) {
    return new ModelAndView("osd/permissionmanagement/addTeamMemberPage")
        .addObject("htmlTitle", "Add user to %s".formatted(team.getDisplayName()))
        .addObject("registrationUrl", energyPortalConfiguration.registrationUrl())
        .addObject("form", form)
        .addObject(
            "submitUrl",
            ReverseRouter.route(on(IndustryAddMemberController.class)
                .addMemberToTeamSubmission(team.toTeamId(), form, ReverseRouter.emptyBindingResult()))
        )
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(IndustryTeamManagementController.class).renderMemberList(team.toTeamId()))
        );
  }
}