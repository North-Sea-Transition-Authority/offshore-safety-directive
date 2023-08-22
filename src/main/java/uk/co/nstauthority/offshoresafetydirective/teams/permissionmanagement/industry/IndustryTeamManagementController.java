package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsMemberOfTeamOrHasRegulatorRole;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Controller
@RequestMapping("/permission-management/industry")
@IsMemberOfTeamOrHasRegulatorRole(RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER)
public class IndustryTeamManagementController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.INDUSTRY;

  private final PermissionService permissionService;
  private final UserDetailService userDetailService;
  private final TeamMemberViewService teamMemberViewService;

  @Autowired
  public IndustryTeamManagementController(TeamService teamService, PermissionService permissionService,
                                          UserDetailService userDetailService,
                                          TeamMemberViewService teamMemberViewService) {
    super(teamService);
    this.permissionService = permissionService;
    this.userDetailService = userDetailService;
    this.teamMemberViewService = teamMemberViewService;
  }

  @GetMapping("/{teamId}")
  public ModelAndView renderMemberList(@PathVariable("teamId") TeamId teamId) {
    var user = userDetailService.getUserDetail();
    var team = getTeam(teamId, TEAM_TYPE);
    var modelAndView = new ModelAndView("osd/permissionmanagement/teamMembersPage")
        .addObject("teamName", team.getDisplayName())
        .addObject("teamRoles", IndustryTeamRole.values())
        .addObject("teamMembers", teamMemberViewService.getTeamMemberViewsForTeam(team));

    if (permissionService.hasPermissionForTeam(teamId, user, Set.of(RolePermission.GRANT_ROLES))
        || permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS))) {
      modelAndView
          .addObject("addTeamMemberUrl",
              ReverseRouter.route(on(IndustryAddMemberController.class).renderAddTeamMember(teamId)))
          .addObject("canRemoveUsers", true)
          .addObject("canEditUsers", true);
    }
    return modelAndView;
  }

}
