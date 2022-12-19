package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination")
@HasPermission(permissions = RolePermission.CREATE_NOMINATION)
public class ApplicantDetailController {

  static final String PAGE_NAME = "Applicant details";

  private final ApplicantDetailFormService applicantDetailFormService;
  private final NominationService nominationService;
  private final ControllerHelperService controllerHelperService;
  private final NominationDetailService nominationDetailService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  private final ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @Autowired
  public ApplicantDetailController(
      ApplicantDetailFormService applicantDetailFormService,
      NominationService nominationService,
      ControllerHelperService controllerHelperService,
      NominationDetailService nominationDetailService,
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
      ApplicantDetailPersistenceService applicantDetailPersistenceService) {
    this.applicantDetailFormService = applicantDetailFormService;
    this.nominationService = nominationService;
    this.controllerHelperService = controllerHelperService;
    this.nominationDetailService = nominationDetailService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.applicantDetailPersistenceService = applicantDetailPersistenceService;
  }

  @GetMapping("/applicant-details")
  public ModelAndView getNewApplicantDetails() {
    return getCreateApplicantDetailModelAndView(new ApplicantDetailForm());
  }

  @PostMapping("/applicant-details")
  public ModelAndView createApplicantDetails(@ModelAttribute("form") ApplicantDetailForm form,
                                             BindingResult bindingResult) {
    bindingResult = applicantDetailFormService.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getCreateApplicantDetailModelAndView(form),
        form,
        () -> {
          var nominationDetail = nominationService.startNomination();
          applicantDetailPersistenceService.createOrUpdateApplicantDetail(form, nominationDetail);
          return ReverseRouter.redirect(on(NominationTaskListController.class)
              .getTaskList(new NominationId(nominationDetail)));
        }
    );
  }

  @GetMapping("/{nominationId}/applicant-details")
  @HasNominationStatus(statuses = NominationStatus.DRAFT)
  public ModelAndView getUpdateApplicantDetails(@PathVariable("nominationId") NominationId nominationId) {
    var detail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getUpdateApplicantDetailModelAndView(applicantDetailFormService.getForm(detail), nominationId);
  }

  @PostMapping("/{nominationId}/applicant-details")
  @HasNominationStatus(statuses = NominationStatus.DRAFT)
  public ModelAndView updateApplicantDetails(@PathVariable("nominationId") NominationId nominationId,
                                             @ModelAttribute("form") ApplicantDetailForm form,
                                             BindingResult bindingResult) {
    bindingResult = applicantDetailFormService.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getUpdateApplicantDetailModelAndView(form, nominationId),
        form,
        () -> {
          var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
          applicantDetailPersistenceService.createOrUpdateApplicantDetail(form, nominationDetail);
          return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));
        }
    );
  }

  private ModelAndView getCreateApplicantDetailModelAndView(ApplicantDetailForm form) {
    return getBaseFormPageModelAndView(form)
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(ApplicantDetailController.class).createApplicantDetails(form, null))
        )
        .addObject("backLinkUrl", ReverseRouter.route(on(StartNominationController.class).startNomination()));
  }

  private ModelAndView getUpdateApplicantDetailModelAndView(ApplicantDetailForm form, NominationId nominationId) {
    var modelAndView = getBaseFormPageModelAndView(form)
        .addObject(
            "actionUrl",
            ReverseRouter.route(
                on(ApplicantDetailController.class).updateApplicantDetails(nominationId, form, null)
            )
        )
        .addObject("preselectedItems", getPreselectedPortalOrganisation(form));

    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(PAGE_NAME)
        .addWorkAreaBreadcrumb()
        .addBreadcrumb(NominationBreadcrumbUtil.getNominationTaskListBreadcrumb(nominationId))
        .build();
    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);
    return modelAndView;
  }

  private ModelAndView getBaseFormPageModelAndView(ApplicantDetailForm form) {
    return new ModelAndView("osd/nomination/applicantdetails/applicantDetails")
        .addObject("form", form)
        .addObject("portalOrganisationsRestUrl", getPortalOrganisationSearchUrl());
  }

  private String getPortalOrganisationSearchUrl() {
    return RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
        .searchPortalOrganisations(null));
  }

  private Map<String, String> getPreselectedPortalOrganisation(ApplicantDetailForm form) {
    var selectedItem = new HashMap<String, String>();
    if (form.getPortalOrganisationId() != null) {
      portalOrganisationUnitQueryService.getOrganisationById(form.getPortalOrganisationId())
          .ifPresent(organisationView -> selectedItem.put(
              String.valueOf(organisationView.id()),
              organisationView.name()
          ));
    }
    return selectedItem;
  }
}
