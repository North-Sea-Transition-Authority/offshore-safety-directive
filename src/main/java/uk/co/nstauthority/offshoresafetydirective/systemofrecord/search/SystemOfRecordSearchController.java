package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@Controller
@RequestMapping("/system-of-record")
@Unauthenticated
public class SystemOfRecordSearchController {

  private static final String OPERATORS_MODEL_AND_VIEW_NAME =
      "osd/systemofrecord/search/operator/searchAppointmentsByOperator";

  private static final String INSTALLATIONS_MODEL_AND_VIEW_NAME =
      "osd/systemofrecord/search/installation/searchInstallationAppointments";

  private static final String WELLBORES_MODEL_AND_VIEW_NAME =
      "osd/systemofrecord/search/well/searchWellAppointments";

  private static final String FORWARD_APPROVALS_MODEL_AND_VIEW_NAME =
      "osd/systemofrecord/search/forwardapproval/searchForwardAreaApprovalAppointments";

  private static final String APPOINTMENTS_MODEL_ATTRIBUTE_NAME = "appointments";

  private static final String HAS_ADDED_FILTER_MODEL_ATTRIBUTE_NAME = "hasAddedFilter";

  private final AppointmentSearchService appointmentSearchService;

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  public SystemOfRecordSearchController(AppointmentSearchService appointmentSearchService,
                                        PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.appointmentSearchService = appointmentSearchService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  @GetMapping("/operators")
  public ModelAndView renderOperatorSearch(
      @Nullable @ModelAttribute("searchForm") SystemOfRecordSearchForm searchForm
  ) {

    if (searchForm == null) {
      searchForm = new SystemOfRecordSearchForm();
    }

    List<AppointmentSearchItemDto> appointments = (searchForm.isEmpty())
        ? Collections.emptyList()
        : appointmentSearchService.searchAppointments(searchForm);

    return getBaseSearchModelAndView(OPERATORS_MODEL_AND_VIEW_NAME, searchForm)
        .addObject(APPOINTMENTS_MODEL_ATTRIBUTE_NAME, appointments);
  }

  @PostMapping("/operators")
  public ModelAndView searchOperatorAppointments(@ModelAttribute("searchForm") SystemOfRecordSearchForm searchForm,
                                                 RedirectAttributes redirectAttributes) {
    if (redirectAttributes != null) {
      redirectAttributes.addFlashAttribute("searchForm", searchForm);
    }
    return ReverseRouter.redirect(on(SystemOfRecordSearchController.class).renderOperatorSearch(null));
  }

  @GetMapping("/installations")
  public ModelAndView renderInstallationSearch() {
    var searchForm = new SystemOfRecordSearchForm();
    return getBaseSearchModelAndView(INSTALLATIONS_MODEL_AND_VIEW_NAME, searchForm)
        .addObject(
            APPOINTMENTS_MODEL_ATTRIBUTE_NAME,
            appointmentSearchService.searchInstallationAppointments(searchForm)
        )
        .addObject(HAS_ADDED_FILTER_MODEL_ATTRIBUTE_NAME, true);
  }

  @GetMapping("/wells")
  public ModelAndView renderWellSearch() {
    var searchForm = new SystemOfRecordSearchForm();
    return getBaseSearchModelAndView(WELLBORES_MODEL_AND_VIEW_NAME, searchForm)
        .addObject(
            APPOINTMENTS_MODEL_ATTRIBUTE_NAME,
            appointmentSearchService.searchWellboreAppointments(searchForm)
        )
        .addObject(HAS_ADDED_FILTER_MODEL_ATTRIBUTE_NAME, true);
  }

  @GetMapping("/forward-area-approvals")
  public ModelAndView renderForwardAreaApprovalSearch() {
    var searchForm = new SystemOfRecordSearchForm();
    return getBaseSearchModelAndView(FORWARD_APPROVALS_MODEL_AND_VIEW_NAME, searchForm)
        .addObject(
            APPOINTMENTS_MODEL_ATTRIBUTE_NAME,
            appointmentSearchService.searchForwardApprovalAppointments(searchForm)
        )
        .addObject(HAS_ADDED_FILTER_MODEL_ATTRIBUTE_NAME, true);
  }

  private ModelAndView getBaseSearchModelAndView(String modelAndViewName, SystemOfRecordSearchForm searchForm) {

    PortalOrganisationDto filteredAppointedOperator = null;

    if (searchForm.getAppointedOperatorId() != null) {
      filteredAppointedOperator = portalOrganisationUnitQueryService
          .getOrganisationById(searchForm.getAppointedOperatorId())
          .orElse(null);
    }

    return new ModelAndView(modelAndViewName)
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage())
        )
        .addObject(APPOINTMENTS_MODEL_ATTRIBUTE_NAME, Collections.emptyList())
        .addObject("searchForm", searchForm)
        .addObject(HAS_ADDED_FILTER_MODEL_ATTRIBUTE_NAME, !searchForm.isEmpty())
        .addObject("filteredAppointedOperator", filteredAppointedOperator)
        .addObject(
            "appointedOperatorRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null))
        );
  }
}
