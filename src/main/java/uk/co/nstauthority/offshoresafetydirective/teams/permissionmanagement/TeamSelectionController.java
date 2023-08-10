package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Controller
@RequestMapping("/permission-management/teams")
@AccessibleByServiceUsers
class TeamSelectionController {

  private final UserDetailService userDetailService;
  private final TeamService teamService;
  private final TeamManagementService teamManagementService;

  @Autowired
  TeamSelectionController(UserDetailService userDetailService, TeamService teamService,
                          TeamManagementService teamManagementService) {
    this.userDetailService = userDetailService;
    this.teamService = teamService;
    this.teamManagementService = teamManagementService;
  }

  @GetMapping
  public ModelAndView renderTeamList(@RequestParam("type") String type) {
    var teamType = TeamType.getTeamTypeFromUrlSlug(type);
    if (teamType.isEmpty()) {
      // If TeamType is unresolvable then redirect the user back to the team type selection screen
      return ReverseRouter.redirect(on(TeamTypeSelectionController.class).renderTeamTypeSelection());
    }

    var user = userDetailService.getUserDetail();
    var teams = teamService.getUserAccessibleTeamsOfType(user, teamType.get());

    if (teams.isEmpty()) {
      return ReverseRouter.redirect(on(TeamTypeSelectionController.class).renderTeamTypeSelection());
    }

    var teamViews = teamManagementService.teamsToTeamViews(teams);

    if (teamViews.size() == 1) {
      return new ModelAndView("redirect:%s".formatted(teamViews.get(0).teamUrl()));
    }

    return new ModelAndView("osd/permissionmanagement/teamSelection")
        .addObject("pageTitle", "Select a team")
        .addObject("teamViews", teamViews)
        .addObject("teamType", teamType.get());
  }

}
