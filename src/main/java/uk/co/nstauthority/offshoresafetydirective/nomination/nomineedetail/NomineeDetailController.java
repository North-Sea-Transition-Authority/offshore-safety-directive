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
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.NominationBreadcrumbUtil;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/nominee-details")
@HasNominationStatus(statuses = NominationStatus.DRAFT)
@HasPermission(permissions = RolePermission.CREATE_NOMINATION)
public class NomineeDetailController {

  static final String PAGE_NAME = "Nominee details";

  private final ControllerHelperService controllerHelperService;
  private final NominationDetailService nominationDetailService;
  private final NomineeDetailFormService nomineeDetailFormService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;

  @Autowired
  public NomineeDetailController(
      ControllerHelperService controllerHelperService,
      NominationDetailService nominationDetailService,
      NomineeDetailFormService nomineeDetailFormService,
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
      NomineeDetailPersistenceService nomineeDetailPersistenceService) {
    this.controllerHelperService = controllerHelperService;
    this.nominationDetailService = nominationDetailService;
    this.nomineeDetailFormService = nomineeDetailFormService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
  }

  @GetMapping
  public ModelAndView getNomineeDetail(@PathVariable("nominationId") NominationId nominationId) {
    var detail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getModelAndView(nomineeDetailFormService.getForm(detail), nominationId);
  }

  @PostMapping
  public ModelAndView saveNomineeDetail(@PathVariable("nominationId") NominationId nominationId,
                                        @ModelAttribute("form") NomineeDetailForm form,
                                        BindingResult bindingResult) {
    return controllerHelperService.checkErrorsAndRedirect(
        nomineeDetailFormService.validate(form, bindingResult),
        getModelAndView(form, nominationId),
        form,
        () -> {
          var detail = nominationDetailService.getLatestNominationDetail(nominationId);
          nomineeDetailPersistenceService.createOrUpdateNomineeDetail(detail, form);
          return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));
        }
    );
  }

  private ModelAndView getModelAndView(NomineeDetailForm form,
                                       NominationId nominationId) {
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
        .addBreadcrumb(NominationBreadcrumbUtil.getNominationTaskListBreadcrumb(nominationId))
        .build();
    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);
    return modelAndView;
  }

  private Map<String, String> getPreselectedPortalOrganisation(NomineeDetailForm form) {
    var selectedItem = new HashMap<String, String>();
    if (form.getNominatedOrganisationId() != null) {
      portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId())
          .ifPresent(organisationView -> selectedItem.put(
              String.valueOf(organisationView.id()),
              organisationView.name()
          ));
    }
    return selectedItem;
  }

  private String getPortalOrganisationSearchUrl() {
    return RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
        .searchPortalOrganisations(null));
  }
}
