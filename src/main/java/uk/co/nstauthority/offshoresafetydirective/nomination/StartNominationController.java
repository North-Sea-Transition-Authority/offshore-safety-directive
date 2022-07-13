package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailController;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@Controller
@RequestMapping("/start-nomination")
public class StartNominationController {

  @GetMapping
  public ModelAndView getStartPage() {
    return new ModelAndView("osd/nomination/startNomination")
        .addObject("startActionUrl", ReverseRouter.route(on(StartNominationController.class).startNomination()))
        .addObject("backLinkUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()));
  }

  @PostMapping
  public ModelAndView startNomination() {
    return ReverseRouter.redirect(on(ApplicantDetailController.class).getApplicantDetails());
  }
}
