package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Controller
@RequestMapping("/nomination/{nominationId}/nominee-details")
public class NomineeDetailController {

  static final String PAGE_NAME = "Nominee details";

  private final ControllerHelperService controllerHelperService;
  private final NominationDetailService nominationDetailService;
  private final NomineeDetailService nomineeDetailService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  public NomineeDetailController(
      ControllerHelperService controllerHelperService,
      NominationDetailService nominationDetailService,
      NomineeDetailService nomineeDetailService,
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.controllerHelperService = controllerHelperService;
    this.nominationDetailService = nominationDetailService;
    this.nomineeDetailService = nomineeDetailService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  @GetMapping
  public ModelAndView getNomineeDetail(@PathVariable("nominationId") Integer nominationId) {
    var detail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getModelAndView(nomineeDetailService.getForm(detail), nominationId);
  }

  @PostMapping
  public ModelAndView saveNomineeDetail(@PathVariable("nominationId") Integer nominationId,
                                        @ModelAttribute("form") NomineeDetailForm form,
                                        BindingResult bindingResult) {
    return controllerHelperService.checkErrorsAndRedirect(
        nomineeDetailService.validate(form, bindingResult),
        getModelAndView(form, nominationId),
        form,
        () -> {
          var detail = nominationDetailService.getLatestNominationDetail(nominationId);
          nomineeDetailService.createOrUpdateNomineeDetail(detail, form);
          return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList());
        }
    );
  }

  private ModelAndView getModelAndView(NomineeDetailForm form,
                                       Integer nominationId) {
    var modelAndView = new ModelAndView("osd/nomination/nomineeDetails/nomineeDetail")
        .addObject("form", form)
        .addObject("pageTitle", PAGE_NAME)
        .addObject("portalOrganisationsRestUrl", getPortalOrganisationSearchUrl())
        .addObject("preselectedItems", getPreselectedPortalOrganisation(form))
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, form, null))
        );
    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(PAGE_NAME)
        .addWorkAreaBreadcrumb()
        .addTaskListBreadcrumb()
        .build();
    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);
    return modelAndView;
  }

  private Map<String, String> getPreselectedPortalOrganisation(NomineeDetailForm form) {
    var selectedItem = new HashMap<String, String>();
    if (form.getNominatedOrganisationId() != null) {
      portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId())
          .ifPresent(organisationView -> selectedItem.put(organisationView.id(), organisationView.name()));
    }
    return selectedItem;
  }

  private String getPortalOrganisationSearchUrl() {
    return PortalOrganisationUnitRestController.route(on(PortalOrganisationUnitRestController.class)
        .searchPortalOrganisations(null));
  }
}
