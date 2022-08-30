package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@Controller
@RequestMapping("nomination/{nominationId}/submission-confirmation")
public class NominationSubmitConfirmationController {

  @GetMapping
  public ModelAndView getSubmissionConfirmationPage(@PathVariable("nominationId") NominationId nominationId) {
    return getModelAndView();
  }

  private ModelAndView getModelAndView() {
    return new ModelAndView("osd/nomination/submission/submissionConfirmation")
        .addObject("workAreaLink", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()));
  }
}
