package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@Controller
@RequestMapping("/system-of-record")
@Unauthenticated
public class SystemOfRecordLandingPageController {

  @GetMapping
  public ModelAndView renderLandingPage() {
    return getLandingPageModelAndView();
  }

  private ModelAndView getLandingPageModelAndView() {
    return new ModelAndView("osd/systemofrecord/search/systemOfRecordLandingPage")
        .addObject(
            "operatorSearchUrl",
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch(null))
        )
        .addObject(
            "installationSearchUrl",
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch(null))
        )
        .addObject(
            "wellSearchUrl",
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch(null))
        )
        .addObject(
            "forwardAreaApprovalSearchUrl",
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderForwardAreaApprovalSearch())
        );
  }
}
