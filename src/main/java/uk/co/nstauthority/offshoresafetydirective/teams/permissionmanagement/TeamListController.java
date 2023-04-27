package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
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
import uk.co.nstauthority.offshoresafetydirective.streamutil.StreamUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamView;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamManagementController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamManagementController;

@Controller
@RequestMapping("/permission-management")
@AccessibleByServiceUsers
public class TeamListController {

  private final UserDetailService userDetailService;
  private final TeamService teamService;
  private final TeamManagementService teamManagementService;

  @Autowired
  public TeamListController(UserDetailService userDetailService,
                            TeamService teamService,
                            TeamManagementService teamManagementService) {
    this.userDetailService = userDetailService;
    this.teamService = teamService;
    this.teamManagementService = teamManagementService;
  }

  @GetMapping
  public ModelAndView resolveTeamListEntryRoute() {
    var user = userDetailService.getUserDetail();
    var teams = teamService.getUserAccessibleTeams(user);
    if (teams.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User [%s] is not in a team".formatted(user.wuaId()));
    } else if (teams.size() == 1) {
      return getSingleTeamRedirect(teams.get(0));
    } else {
      return ReverseRouter.redirect(on(TeamListController.class).renderTeamList());
    }
  }

  private ModelAndView getSingleTeamRedirect(Team team) {
    return switch (team.getTeamType()) {
      case REGULATOR ->
          ReverseRouter.redirect(on(RegulatorTeamManagementController.class).renderMemberList(team.toTeamId()));
      case CONSULTEE ->
          ReverseRouter.redirect(on(ConsulteeTeamManagementController.class).renderMemberList(team.toTeamId()));
    };
  }

  @GetMapping("/teams")
  public ModelAndView renderTeamList() {
    var teams = teamService.getUserAccessibleTeams(userDetailService.getUserDetail());
    var teamViews = teamManagementService.teamsToTeamViews(teams);

    var teamViewMap = teamViews.stream()
        .collect(Collectors.groupingBy(TeamView::teamType))
        .entrySet()
        .stream()
        .sorted(Comparator.comparing(entry -> entry.getKey().getDisplayOrder()))
        .collect(StreamUtil.toLinkedHashMap(Map.Entry::getKey, Map.Entry::getValue));

    return new ModelAndView("osd/permissionmanagement/teamSelectionPage")
        .addObject("pageTitle", "Select a team")
        .addObject("teamGroupMap", teamViewMap);
  }

}
