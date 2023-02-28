package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@Controller
@RequestMapping("/system-of-record")
@Unauthenticated
public class SystemOfRecordSearchController {

  private static final String BACK_LINK_MODEL_PROPERTY_NAME = "backLinkUrl";

  private final AppointmentSearchService appointmentSearchService;

  @Autowired
  public SystemOfRecordSearchController(AppointmentSearchService appointmentSearchService) {
    this.appointmentSearchService = appointmentSearchService;
  }

  @GetMapping("/operators")
  public ModelAndView renderOperatorSearch() {
    return new ModelAndView("osd/systemofrecord/search/operator/searchAppointmentsByOperator")
        .addObject(BACK_LINK_MODEL_PROPERTY_NAME, getSystemOfRecordLandingPageUrl())
        .addObject("appointments", appointmentSearchService.searchAppointments());
  }

  @GetMapping("/installations")
  public ModelAndView renderInstallationSearch() {
    return new ModelAndView("osd/systemofrecord/search/installation/searchInstallationAppointments")
        .addObject(BACK_LINK_MODEL_PROPERTY_NAME, getSystemOfRecordLandingPageUrl())
        .addObject("appointments", appointmentSearchService.searchInstallationAppointments());
  }

  @GetMapping("/wells")
  public ModelAndView renderWellSearch() {
    return new ModelAndView("osd/systemofrecord/search/well/searchWellAppointments")
        .addObject(BACK_LINK_MODEL_PROPERTY_NAME, getSystemOfRecordLandingPageUrl())
        .addObject("appointments", appointmentSearchService.searchWellboreAppointments());
  }

  @GetMapping("/forward-area-approvals")
  public ModelAndView renderForwardAreaApprovalSearch() {
    return new ModelAndView(
        "osd/systemofrecord/search/forwardapproval/searchForwardAreaApprovalAppointments"
    )
        .addObject(BACK_LINK_MODEL_PROPERTY_NAME, getSystemOfRecordLandingPageUrl())
        .addObject("appointments", appointmentSearchService.searchForwardApprovalAppointments());
  }

  private String getSystemOfRecordLandingPageUrl() {
    return ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage());
  }
}
