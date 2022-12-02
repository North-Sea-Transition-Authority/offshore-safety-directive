package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;

@Controller
@RequestMapping("/work-area")
@AccessibleByServiceUsers
public class WorkAreaController {

  public static final String WORK_AREA_TITLE = "Work area";

  private final WorkAreaItemService workAreaItemService;

  @Autowired
  public WorkAreaController(WorkAreaItemService workAreaItemService) {
    this.workAreaItemService = workAreaItemService;
  }

  @GetMapping
  public ModelAndView getWorkArea() {
    return new ModelAndView("osd/workarea/workArea")
        .addObject("startNominationUrl", ReverseRouter.route(on(StartNominationController.class).startNomination()))
        .addObject("workAreaItems", workAreaItemService.getWorkAreaItems());
  }
}
