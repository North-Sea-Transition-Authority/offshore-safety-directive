package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.NominationRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Controller
@RequestMapping("/work-area")
@AccessibleByServiceUsers
@SessionAttributes("workAreaSession")
public class WorkAreaController {

  public static final String WORK_AREA_TITLE = "Work area";

  private final WorkAreaItemService workAreaItemService;

  private final UserDetailService userDetailService;

  private final NominationRoleService nominationRoleService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public WorkAreaController(
      WorkAreaItemService workAreaItemService,
      UserDetailService userDetailService,
      NominationRoleService nominationRoleService,
      TeamQueryService teamQueryService
  ) {
    this.workAreaItemService = workAreaItemService;
    this.userDetailService = userDetailService;
    this.nominationRoleService = nominationRoleService;
    this.teamQueryService = teamQueryService;
  }

  @GetMapping
  public ModelAndView getWorkArea(
      @ModelAttribute("workAreaSession") WorkAreaSession workAreaSession
  ) {
    var statusMap = NominationStatus.getDisplayNameByEnumName(workAreaSession.canSeeDrafts());

    var filter = workAreaSession.getWorkAreaFilter();

    var modelAndView = new ModelAndView("osd/workarea/workArea")
        .addObject("form", WorkAreaFilterForm.fromFilter(filter))
        .addObject("statusMap", statusMap)
        .addObject("workAreaItems", workAreaItemService.getWorkAreaItems(filter))
        .addObject("clearFiltersUrl", ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilters(null, null)));

    var canUserStartNomination = nominationRoleService.userCanStartNomination(userDetailService.getUserDetail().wuaId());

    if (canUserStartNomination) {
      modelAndView.addObject(
          "startNominationUrl",
          ReverseRouter.route(on(StartNominationController.class).startNomination())
      );
    }
    return modelAndView;
  }

  @PostMapping
  public ModelAndView renderWorkAreaResults(
      @ModelAttribute("form") WorkAreaFilterForm form,
      @ModelAttribute("workAreaSession") WorkAreaSession workAreaSession
  ) {
    workAreaSession.updateSession(WorkAreaFilter.fromForm(form));
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null));
  }

  @GetMapping("/clear-filters")
  public ModelAndView clearWorkAreaFilters(
      @ModelAttribute("workAreaSession") WorkAreaSession workAreaSession,
      SessionStatus sessionStatus
  ) {
    sessionStatus.setComplete();
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null));
  }

  @ModelAttribute("workAreaSession")
  WorkAreaSession getWorkAreaSessionWithDefaultFilters() {
    var wuaId = userDetailService.getUserDetail().wuaId();
    var canSeeDrafts = teamQueryService.getTeamRolesForUser(wuaId).stream()
        .filter(teamRole -> teamRole.getTeam().getTeamType() == TeamType.ORGANISATION_GROUP)
        .map(TeamRole::getRole)
        .anyMatch(role -> role == Role.NOMINATION_SUBMITTER || role == Role.NOMINATION_EDITOR);
    return new WorkAreaSession(WorkAreaFilter.defaultFilter(canSeeDrafts), canSeeDrafts);
  }
}
