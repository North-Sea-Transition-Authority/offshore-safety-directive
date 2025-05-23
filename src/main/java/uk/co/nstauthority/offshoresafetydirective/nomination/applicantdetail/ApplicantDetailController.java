package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.NominationBreadcrumbUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.OrganisationFilterType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.CanAccessDraftNomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.CanStartNomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@Controller
@RequestMapping("/nomination")
public class ApplicantDetailController {

  static final String PAGE_NAME = "Applicant details";
  static final RequestPurpose PRE_SELECTED_APPLICANT_ORGANISATION_PURPOSE =
      new RequestPurpose("Get pre-selected organisation");

  private final ApplicantDetailFormService applicantDetailFormService;
  private final NominationService nominationService;
  private final NominationDetailService nominationDetailService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  private final ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @Autowired
  public ApplicantDetailController(
      ApplicantDetailFormService applicantDetailFormService,
      NominationService nominationService,
      NominationDetailService nominationDetailService,
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
      ApplicantDetailPersistenceService applicantDetailPersistenceService) {
    this.applicantDetailFormService = applicantDetailFormService;
    this.nominationService = nominationService;
    this.nominationDetailService = nominationDetailService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.applicantDetailPersistenceService = applicantDetailPersistenceService;
  }

  @GetMapping("/applicant-details")
  @CanStartNomination
  public ModelAndView getNewApplicantDetails() {
    return getCreateApplicantDetailModelAndView(new ApplicantDetailForm());
  }

  @PostMapping("/applicant-details")
  @CanStartNomination
  public ModelAndView createApplicantDetails(@ModelAttribute("form") ApplicantDetailForm form,
                                             BindingResult bindingResult) {
    bindingResult = applicantDetailFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getCreateApplicantDetailModelAndView(form);
    }

    var nominationDetail = nominationService.startNomination();
    applicantDetailPersistenceService.createOrUpdateApplicantDetail(form, nominationDetail);
    return ReverseRouter.redirect(on(NominationTaskListController.class)
        .getTaskList(new NominationId(nominationDetail)));
  }

  @GetMapping("/{nominationId}/applicant-details")
  @CanAccessDraftNomination
  public ModelAndView getUpdateApplicantDetails(@PathVariable("nominationId") NominationId nominationId) {
    var detail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getUpdateApplicantDetailModelAndView(applicantDetailFormService.getForm(detail), nominationId);
  }

  @PostMapping("/{nominationId}/applicant-details")
  @CanAccessDraftNomination
  public ModelAndView updateApplicantDetails(@PathVariable("nominationId") NominationId nominationId,
                                             @ModelAttribute("form") ApplicantDetailForm form,
                                             BindingResult bindingResult) {
    bindingResult = applicantDetailFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getUpdateApplicantDetailModelAndView(form, nominationId);
    }

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    applicantDetailPersistenceService.createOrUpdateApplicantDetail(form, nominationDetail);
    return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));

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
        .searchOrganisationsRelatedToUser(null, OrganisationFilterType.ACTIVE.name(), null));
  }

  private Map<String, String> getPreselectedPortalOrganisation(ApplicantDetailForm form) {
    var selectedItem = new HashMap<String, String>();
    Integer organisationId;

    if (form.getPortalOrganisationId() != null) {
      try {
        organisationId = Integer.valueOf(form.getPortalOrganisationId());
      } catch (NumberFormatException ignored) {
        organisationId = null;
      }

      if (organisationId != null) {
        portalOrganisationUnitQueryService.getOrganisationById(organisationId, PRE_SELECTED_APPLICANT_ORGANISATION_PURPOSE)
            .ifPresent(portalOrganisationDto -> selectedItem.put(
                Objects.toString(portalOrganisationDto.id(), null),
                OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(portalOrganisationDto)
            ));
      }
    }
    return selectedItem;
  }
}
