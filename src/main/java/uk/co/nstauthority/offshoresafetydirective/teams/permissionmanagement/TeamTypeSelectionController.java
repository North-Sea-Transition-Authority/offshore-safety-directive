package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
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

    if (teamTypes.keySet().size() == 1) {
      // Redirect to next screen as only one team type to select
      var teamType = teamTypes.keySet()
          .stream()
          .findFirst()
          .orElseThrow(() ->
              new IllegalStateException("Expected keySet to have a size of 1 but unable to find a TeamType")
          );
      return ReverseRouter.redirect(on(TeamSelectionController.class).renderTeamList(teamType.getUrlSlug()));
    }

    return new ModelAndView("osd/permissionmanagement/teamTypeSelection")
        .addObject("pageTitle", "Select a team")
        .addObject("teamTypeRouteMap", teamTypes);
  }

}
