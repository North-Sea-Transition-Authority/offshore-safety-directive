package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionForm;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionValidationHint;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionValidator;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.NominationReferenceRestController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping
@HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
public class NewAppointmentController {

  private final PortalAssetRetrievalService portalAssetRetrievalService;
  private final AppointmentCorrectionService appointmentCorrectionService;
  private final AppointmentService appointmentService;
  private final AssetPersistenceService assetPersistenceService;
  private final ControllerHelperService controllerHelperService;
  private final AppointmentCorrectionValidator appointmentCorrectionValidator;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public NewAppointmentController(PortalAssetRetrievalService portalAssetRetrievalService,
                                  AppointmentCorrectionService appointmentCorrectionService,
                                  AppointmentService appointmentService,
                                  AssetPersistenceService assetPersistenceService,
                                  ControllerHelperService controllerHelperService,
                                  AppointmentCorrectionValidator appointmentCorrectionValidator,
                                  PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                  NominationDetailService nominationDetailService) {
    this.portalAssetRetrievalService = portalAssetRetrievalService;
    this.appointmentCorrectionService = appointmentCorrectionService;
    this.appointmentService = appointmentService;
    this.assetPersistenceService = assetPersistenceService;
    this.controllerHelperService = controllerHelperService;
    this.appointmentCorrectionValidator = appointmentCorrectionValidator;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping("/installation/{portalAssetId}/appointments/add")
  public ModelAndView renderNewInstallationAppointment(@PathVariable PortalAssetId portalAssetId) {
    var assetType = PortalAssetType.INSTALLATION;
    return getNewAppointmentForm(portalAssetId, assetType, new AppointmentCorrectionForm());
  }

  @PostMapping("/installation/{portalAssetId}/appointments/add")
  public ModelAndView createNewInstallationAppointment(@PathVariable PortalAssetId portalAssetId,
                                                       @ModelAttribute("form") AppointmentCorrectionForm form,
                                                       BindingResult bindingResult) {
    var assetType = PortalAssetType.INSTALLATION;
    var assetDto = assetPersistenceService.getOrCreateAsset(portalAssetId, assetType);

    var validatorHint = new AppointmentCorrectionValidationHint(
        null,
        assetDto.assetId(),
        assetType
    );

    appointmentCorrectionValidator.validate(form, bindingResult, validatorHint);

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getNewAppointmentForm(portalAssetId, assetType, form),
        form,
        () -> {
          appointmentService.addManualAppointment(form, assetDto);
          return switch (assetType) {
            case INSTALLATION -> ReverseRouter.redirect(on(AssetTimelineController.class)
                .renderInstallationTimeline(portalAssetId));
            // TODO OSDOP-158 - Redirect to relevant endpoints
            default -> throw new NotImplementedException();
          };
        }
    );
  }

  private ModelAndView getNewAppointmentForm(PortalAssetId portalAssetId, PortalAssetType portalAssetType,
                                             AppointmentCorrectionForm form) {

    var assetName = portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No portal asset of type [%s] found with ID [%s]".formatted(
                portalAssetType,
                portalAssetId.id()
            )));
    var appointmentTypes = DisplayableEnumOptionUtil.getDisplayableOptions(AppointmentType.class);
    var modelAndView = new ModelAndView("osd/systemofrecord/correction/correctAppointment")
        .addObject("pageTitle", "Add appointment")
        .addObject("assetName", assetName)
        .addObject("assetTypeDisplayName", portalAssetType.getDisplayName())
        .addObject("assetTypeSentenceCaseDisplayName", portalAssetType.getSentenceCaseDisplayName())
        .addObject("submitUrl", ReverseRouter.route(on(NewAppointmentController.class)
            .createNewInstallationAppointment(portalAssetId, null, null)))
        .addObject("portalOrganisationsRestUrl", RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
            .searchAllPortalOrganisations(null)))
        .addObject("phases", appointmentCorrectionService.getSelectablePhaseMap(portalAssetType))
        .addObject("appointmentTypes", appointmentTypes)
        .addObject("form", form)
        .addObject("cancelUrl", getTimelineRoute(portalAssetId, portalAssetType))
        .addObject(
            "nominationReferenceRestUrl",
            RestApiUtil.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(null))
        );

    if (form.getAppointedOperatorId() != null) {
      modelAndView.addObject("preselectedOperator", getPreselectedOperator(form));
    }

    if (AppointmentType.ONLINE_NOMINATION.name().equals(form.getAppointmentType())
        && form.getOnlineNominationReference() != null) {
      nominationDetailService.getLatestNominationDetailOptional(
          new NominationId(UUID.fromString(form.getOnlineNominationReference()))
      ).ifPresent(nominationDetail -> {
        var nomination = nominationDetail.getNomination();
        modelAndView.addObject("preselectedNominationReference", Map.of(
            nomination.getId(),
            nomination.getReference()
        ));
      });
    }

    if (PortalAssetType.SUBAREA.equals(portalAssetType)) {
      modelAndView.addObject("phaseSelectionHint", "If decommissioning is required, another phase must be selected.");
    }

    return modelAndView;
  }

  String getTimelineRoute(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    return switch (portalAssetType) {
      case WELLBORE -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderWellboreTimeline(portalAssetId));
      case INSTALLATION -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderInstallationTimeline(portalAssetId));
      case SUBAREA -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderSubareaTimeline(portalAssetId));
    };
  }

  private Map<String, String> getPreselectedOperator(AppointmentCorrectionForm form) {
    var operator = portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId());
    return operator.stream()
        .collect(Collectors.toMap(
            portalOrganisationDto -> String.valueOf(portalOrganisationDto.id()),
            OrganisationUnitDisplayUtil::getOrganisationUnitDisplayName
        ));
  }

}
