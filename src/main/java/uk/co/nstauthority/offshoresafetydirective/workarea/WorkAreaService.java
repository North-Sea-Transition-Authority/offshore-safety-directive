package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;

@Service
class WorkAreaService {

  ModelAndView getModelAndView() {
    return new ModelAndView("osd/workarea/workArea")
        .addObject("startNominationUrl", ReverseRouter.route(on(StartNominationController.class).startNomination()));
  }
}
