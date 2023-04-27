package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsMemberOfTeam;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AbstractTeamController;

@Controller
@RequestMapping("/permission-management/regulator")
public class RegulatorTeamManagementController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.REGULATOR;

  private final TeamMemberViewService teamMemberViewService;
  private final RegulatorTeamService regulatorTeamService;
  private final UserDetailService userDetailService;
  private final CustomerConfigurationProperties customerConfigurationProperties;
  private final TeamMemberService teamMemberService;

  @Autowired
  RegulatorTeamManagementController(TeamMemberViewService teamMemberViewService,
                                    RegulatorTeamService regulatorTeamService,
                                    UserDetailService userDetailService,
                                    CustomerConfigurationProperties customerConfigurationProperties,
                                    TeamMemberService teamMemberService,
                                    TeamService teamService) {
    super(teamService);
    this.teamMemberViewService = teamMemberViewService;
    this.regulatorTeamService = regulatorTeamService;
    this.userDetailService = userDetailService;
    this.customerConfigurationProperties = customerConfigurationProperties;
    this.teamMemberService = teamMemberService;
  }

  @GetMapping("/{teamId}")
  @IsMemberOfTeam
  public ModelAndView renderMemberList(@PathVariable("teamId") TeamId teamId) {

    var team = getTeam(teamId, TEAM_TYPE);

    var user = userDetailService.getUserDetail();

    var modelAndView = new ModelAndView("osd/permissionmanagement/teamMembersPage")
        .addObject("pageTitle", "Manage %s".formatted(customerConfigurationProperties.mnemonic()))
        .addObject("teamName", customerConfigurationProperties.mnemonic())
        .addObject("teamRoles", RegulatorTeamRole.values())
        .addObject("teamMembers", teamMemberViewService.getTeamMemberViewsForTeam(team));

    if (regulatorTeamService.isAccessManager(teamId, userDetailService.getUserDetail())) {
      modelAndView
          .addObject("addTeamMemberUrl",
              ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId)))
          .addObject("canRemoveUsers", teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user,
              Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
          .addObject("canEditUsers", teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user,
              Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())));
    }

    return modelAndView;
  }

}