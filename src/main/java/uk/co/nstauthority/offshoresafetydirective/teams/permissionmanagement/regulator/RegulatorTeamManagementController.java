package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberViewService;

@Controller
@RequestMapping("/permission-management/regulator")
public class RegulatorTeamManagementController {

  private final TeamMemberViewService teamMemberViewService;
  private final RegulatorTeamService regulatorTeamService;
  private final UserDetailService userDetailService;
  private final CustomerConfigurationProperties customerConfigurationProperties;

  @Autowired
  RegulatorTeamManagementController(TeamMemberViewService teamMemberViewService,
                                    RegulatorTeamService regulatorTeamService,
                                    UserDetailService userDetailService,
                                    CustomerConfigurationProperties customerConfigurationProperties) {
    this.teamMemberViewService = teamMemberViewService;
    this.regulatorTeamService = regulatorTeamService;
    this.userDetailService = userDetailService;
    this.customerConfigurationProperties = customerConfigurationProperties;
  }

  @GetMapping
  public ModelAndView renderMemberList() {

    var user = userDetailService.getUserDetail();
    var team = regulatorTeamService.getRegulatorTeamForUser(user)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "User with wua id [%d] is not part of a regulator team".formatted(user.wuaId())
        ));

    return new ModelAndView("osd/permissionmanagement/regulatorTeamMembers")
        .addObject("pageTitle", "Manage %s".formatted(customerConfigurationProperties.mnemonic()))
        .addObject("teamName", customerConfigurationProperties.mnemonic())
        .addObject("teamRoles", RegulatorTeamRole.values())
        .addObject("teamMembers", teamMemberViewService.getUserViewsForTeam(team));
  }

}
