package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsMemberOfTeamOrHasRegulatorRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AbstractTeamController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Controller
@RequestMapping("/permission-management/industry")
@IsMemberOfTeamOrHasRegulatorRole(RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER)
public class IndustryTeamManagementController extends AbstractTeamController {

  static final TeamType TEAM_TYPE = TeamType.INDUSTRY;

  @Autowired
  public IndustryTeamManagementController(TeamService teamService) {
    super(teamService);
  }

  @GetMapping("/{teamId}")
  public ModelAndView renderMemberList(@PathVariable("teamId") TeamId teamId) {
    var team = getTeam(teamId, TEAM_TYPE);
    return new ModelAndView("osd/permissionmanagement/teamMembersPage")
        .addObject("teamName", team.getDisplayName())
        .addObject("teamRoles", IndustryTeamRole.values())
        .addObject("teamMembers", List.of());
  }

}
