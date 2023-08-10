package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;

@Controller
@RequestMapping("/permission-management")
@AccessibleByServiceUsers
public class TeamTypeSelectionController {

  private final TeamService teamService;
  private final UserDetailService userDetailService;
  private final TeamManagementService teamManagementService;

  @Autowired
  public TeamTypeSelectionController(TeamService teamService, UserDetailService userDetailService,
                                     TeamManagementService teamManagementService) {
    this.teamService = teamService;
    this.userDetailService = userDetailService;
    this.teamManagementService = teamManagementService;
  }

  @GetMapping
  public ModelAndView renderTeamTypeSelection() {

    var user = userDetailService.getUserDetail();
    var teams = teamService.getUserAccessibleTeams(user);

    if (teams.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "No accessible teams for user [%d]".formatted(user.wuaId())
      );
    }

    var teamTypes = teamManagementService.getManageTeamTypeUrls(teams);

    return new ModelAndView("osd/permissionmanagement/teamTypeSelection")
        .addObject("pageTitle", "Select a team")
        .addObject("teamTypeRouteMap", teamTypes);
  }

}
