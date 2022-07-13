package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;

@Controller
@RequestMapping("/work-area")
public class WorkAreaController {

  public static final String WORK_AREA_TITLE = "Work area";

  @GetMapping
  public ModelAndView getWorkArea() {
    return new ModelAndView("osd/workarea/workArea")
        .addObject("startNominationUrl", ReverseRouter.route(on(StartNominationController.class).startNomination()));
  }
}
