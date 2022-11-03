package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.IsMemberOfTeam;

@Controller
@RequestMapping("/permission-management/regulator")
public class RegulatorTeamManagementController extends AbstractRegulatorPermissionManagement {

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
                                    TeamMemberService teamMemberService) {
    super(regulatorTeamService);
    this.teamMemberViewService = teamMemberViewService;
    this.regulatorTeamService = regulatorTeamService;
    this.userDetailService = userDetailService;
    this.customerConfigurationProperties = customerConfigurationProperties;
    this.teamMemberService = teamMemberService;
  }

  @GetMapping
  public ModelAndView renderMemberListRedirect() {

    var team = regulatorTeamService.getRegulatorTeamForUser(userDetailService.getUserDetail())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "User with ID %s is not a member of regulator team".formatted(userDetailService.getUserDetail())
        ));

    return ReverseRouter.redirect(on(RegulatorTeamManagementController.class)
        .renderMemberList(new TeamId(team.getUuid())));
  }

  @GetMapping("/{teamId}")
  @IsMemberOfTeam
  public ModelAndView renderMemberList(@PathVariable("teamId") TeamId teamId) {

    var team = getRegulatorTeam(teamId);

    var user = userDetailService.getUserDetail();

    var modelAndView = new ModelAndView("osd/permissionmanagement/regulator/regulatorTeamMembers")
        .addObject("pageTitle", "Manage %s".formatted(customerConfigurationProperties.mnemonic()))
        .addObject("teamName", customerConfigurationProperties.mnemonic())
        .addObject("teamRoles", RegulatorTeamRole.values())
        .addObject("teamMembers", teamMemberViewService.getTeamMemberViewsForTeam(team));

    if (regulatorTeamService.isAccessManager(teamId, userDetailService.getUserDetail())) {
      modelAndView.addObject(
          "addTeamMemberUrl",
          ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId))
      );
      modelAndView.addObject("canRemoveUsers", teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user,
          Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())));
    }

    return modelAndView;
  }

}