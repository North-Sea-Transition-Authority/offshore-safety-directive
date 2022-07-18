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
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Controller
@RequestMapping("/nomination")
public class ApplicantDetailController {

  static final String PAGE_NAME = "Applicant details";

  private final ApplicantDetailService applicantDetailService;
  private final NominationService nominationService;
  private final ControllerHelperService controllerHelperService;
  private final NominationDetailService nominationDetailService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  public ApplicantDetailController(
      ApplicantDetailService applicantDetailService,
      NominationService nominationService,
      ControllerHelperService controllerHelperService,
      NominationDetailService nominationDetailService,
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.applicantDetailService = applicantDetailService;
    this.nominationService = nominationService;
    this.controllerHelperService = controllerHelperService;
    this.nominationDetailService = nominationDetailService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  @GetMapping("/applicant-details")
  public ModelAndView getNewApplicantDetails() {
    return getCreateApplicantDetailModelAndView(new ApplicantDetailForm());
  }

  @PostMapping("/applicant-details")
  public ModelAndView createApplicantDetails(@ModelAttribute("form") ApplicantDetailForm form,
                                             BindingResult bindingResult) {
    bindingResult = applicantDetailService.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getCreateApplicantDetailModelAndView(form),
        form,
        () -> {
          var nominationDetail = nominationService.startNomination();
          applicantDetailService.createOrUpdateApplicantDetail(form, nominationDetail);
          return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList());
        }
    );
  }

  @GetMapping("/{nominationId}/applicant-details")
  public ModelAndView getUpdateApplicantDetails(@PathVariable("nominationId") Integer nominationId) {
    var detail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getUpdateApplicantDetailModelAndView(applicantDetailService.getForm(detail), nominationId);
  }

  @PostMapping("/{nominationId}/applicant-details")
  public ModelAndView updateApplicantDetails(@PathVariable("nominationId") Integer nominationId,
                                             @ModelAttribute("form") ApplicantDetailForm form,
                                             BindingResult bindingResult) {
    bindingResult = applicantDetailService.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getUpdateApplicantDetailModelAndView(form, nominationId),
        form,
        () -> {
          var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
          applicantDetailService.createOrUpdateApplicantDetail(form, nominationDetail);
          return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList());
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

  private ModelAndView getUpdateApplicantDetailModelAndView(ApplicantDetailForm form, Integer nominationId) {
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
        .addTaskListBreadcrumb()
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
    return PortalOrganisationUnitRestController.route(on(PortalOrganisationUnitRestController.class)
        .searchPortalOrganisations(null));
  }

  private Map<String, String> getPreselectedPortalOrganisation(ApplicantDetailForm form) {
    var selectedItem = new HashMap<String, String>();
    if (form.getPortalOrganisationId() != null) {
      portalOrganisationUnitQueryService.getOrganisationById(form.getPortalOrganisationId())
          .ifPresent(organisationView -> selectedItem.put(organisationView.id(), organisationView.name()));
    }
    return selectedItem;
  }
}
