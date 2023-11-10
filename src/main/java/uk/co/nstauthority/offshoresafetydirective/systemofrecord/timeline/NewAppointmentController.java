package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasAssetStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionForm;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionValidationHint;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionValidator;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.ForwardApprovedAppointmentRestController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.NominationReferenceRestController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping
@HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
public class NewAppointmentController {

  static final RequestPurpose PRE_SELECTED_FORWARD_APPROVED_APPOINTMENT_PURPOSE =
      new RequestPurpose("Subarea name for the preselected forward approved appointment display string");
  private final PortalAssetRetrievalService portalAssetRetrievalService;
  private final AppointmentCorrectionService appointmentCorrectionService;
  private final AppointmentService appointmentService;
  private final AssetPersistenceService assetPersistenceService;
  private final ControllerHelperService controllerHelperService;
  private final AppointmentCorrectionValidator appointmentCorrectionValidator;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final NominationDetailService nominationDetailService;
  private final AssetAccessService assetAccessService;
  private final AppointmentAccessService appointmentAccessService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  public NewAppointmentController(PortalAssetRetrievalService portalAssetRetrievalService,
                                  AppointmentCorrectionService appointmentCorrectionService,
                                  AppointmentService appointmentService,
                                  AssetPersistenceService assetPersistenceService,
                                  ControllerHelperService controllerHelperService,
                                  AppointmentCorrectionValidator appointmentCorrectionValidator,
                                  PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                  NominationDetailService nominationDetailService,
                                  AssetAccessService assetAccessService, AppointmentAccessService appointmentAccessService,
                                  LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.portalAssetRetrievalService = portalAssetRetrievalService;
    this.appointmentCorrectionService = appointmentCorrectionService;
    this.appointmentService = appointmentService;
    this.assetPersistenceService = assetPersistenceService;
    this.controllerHelperService = controllerHelperService;
    this.appointmentCorrectionValidator = appointmentCorrectionValidator;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.nominationDetailService = nominationDetailService;
    this.assetAccessService = assetAccessService;
    this.appointmentAccessService = appointmentAccessService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  @GetMapping("/asset/{assetId}/appointment/add")
  @HasAssetStatus(AssetStatus.EXTANT)
  public ModelAndView renderNewAppointment(@PathVariable AssetId assetId) {
    var assetDto = assetAccessService.getAsset(assetId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No asset found with asset id [%s]".formatted(assetId.id()
            ))
        );
    return getNewAppointmentForm(assetDto, new AppointmentCorrectionForm());
  }

  @GetMapping("/installation/{portalAssetId}/appointments/add")
  public ModelAndView renderNewInstallationAppointment(@PathVariable PortalAssetId portalAssetId) {
    return getOrCreateAssetIfInPortal(PortalAssetType.INSTALLATION, portalAssetId);
  }

  @GetMapping("/wellbore/{portalAssetId}/appointments/add")
  public ModelAndView renderNewWellboreAppointment(@PathVariable PortalAssetId portalAssetId) {
    return getOrCreateAssetIfInPortal(PortalAssetType.WELLBORE, portalAssetId);
  }

  @GetMapping("/forward-approval/{portalAssetId}/appointments/add")
  public ModelAndView renderNewSubareaAppointment(@PathVariable PortalAssetId portalAssetId) {
    return getOrCreateAssetIfInPortal(PortalAssetType.SUBAREA, portalAssetId);
  }

  @PostMapping("/asset/{assetId}/appointment/add")
  @HasAssetStatus(AssetStatus.EXTANT)
  public ModelAndView createNewAppointment(@PathVariable AssetId assetId,
                                           @ModelAttribute("form") AppointmentCorrectionForm form,
                                           BindingResult bindingResult,
                                           RedirectAttributes redirectAttributes) {
    var asset = assetAccessService.getAsset(assetId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No asset found with asset id [%s]".formatted(assetId.id()
            ))
        );
    return processCreateNewAppointment(asset, form, bindingResult, redirectAttributes);
  }

  private ModelAndView getOrCreateAssetIfInPortal(PortalAssetType portalAssetType, PortalAssetId portalAssetId) {

    if (portalAssetRetrievalService.isExtantInPortal(portalAssetId, portalAssetType)
        || assetAccessService.isAssetExtant(portalAssetId, portalAssetType)) {
      var asset = assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType);
      return ReverseRouter.redirect(on(NewAppointmentController.class).renderNewAppointment(asset.assetId()));
    }

    throw new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "No portal asset found with portal asset id [%s]".formatted(portalAssetId.id())
    );
  }

  private ModelAndView processCreateNewAppointment(AssetDto assetDto,
                                                   AppointmentCorrectionForm form, BindingResult bindingResult,
                                                   RedirectAttributes redirectAttributes) {

    var validatorHint = new AppointmentCorrectionValidationHint(
        null,
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    appointmentCorrectionValidator.validate(form, bindingResult, validatorHint);

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getNewAppointmentForm(assetDto, form),
        form,
        () -> {
          appointmentService.addManualAppointment(form, assetDto);

          var notificationBanner = NotificationBanner.builder()
              .withBannerType(NotificationBannerType.SUCCESS)
              .withHeading("Added appointment for %s".formatted(
                  assetDto.assetName().value()
              ))
              .build();

          NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);

          return switch (assetDto.portalAssetType()) {
            case INSTALLATION -> ReverseRouter.redirect(on(AssetTimelineController.class)
                .renderInstallationTimeline(assetDto.portalAssetId()));
            case WELLBORE -> ReverseRouter.redirect(on(AssetTimelineController.class)
                .renderWellboreTimeline(assetDto.portalAssetId()));
            case SUBAREA -> ReverseRouter.redirect(on(AssetTimelineController.class)
                .renderSubareaTimeline(assetDto.portalAssetId()));
          };
        }
    );
  }

  private ModelAndView getNewAppointmentForm(AssetDto assetDto, AppointmentCorrectionForm form) {

    var assetName = portalAssetRetrievalService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType())
        .orElse(assetDto.assetName().value());
    var appointmentTypes = AppointmentType.getDisplayableOptions(assetDto.portalAssetType());

    var modelAndView = new ModelAndView("osd/systemofrecord/correction/correctAppointment")
        .addObject("pageTitle", "Add appointment")
        .addObject("assetName", assetName)
        .addObject("assetTypeDisplayName", assetDto.portalAssetType().getDisplayName())
        .addObject("assetTypeSentenceCaseDisplayName", assetDto.portalAssetType().getSentenceCaseDisplayName())
        .addObject("submitUrl", ReverseRouter.route(on(NewAppointmentController.class)
            .createNewAppointment(assetDto.assetId(), null, null, null)))
        .addObject("portalOrganisationsRestUrl", RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
            .searchAllPortalOrganisations(null)))
        .addObject("phases", appointmentCorrectionService.getSelectablePhaseMap(assetDto.portalAssetType()))
        .addObject("appointmentTypes", appointmentTypes)
        .addObject("form", form)
        .addObject("cancelUrl", getTimelineRoute(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .addObject(
            "nominationReferenceRestUrl",
            RestApiUtil.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(null))
        )
        .addObject(
            "forwardApprovedAppointmentRestUrl",
            RestApiUtil.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(null))
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

    if (AppointmentType.FORWARD_APPROVED.name().equals(form.getAppointmentType())
        && form.getForwardApprovedAppointmentId() != null) {
      appointmentAccessService.getAppointment(
          new AppointmentId(UUID.fromString(form.getForwardApprovedAppointmentId()))

      ).ifPresent(preSelectedAppointment -> {
        var startDate = DateUtil.formatLongDate(preSelectedAppointment.getResponsibleFromDate());

        var subareaName = licenceBlockSubareaQueryService.getLicenceBlockSubarea(
                new LicenceBlockSubareaId(preSelectedAppointment.getAsset().getPortalAssetId()),
                PRE_SELECTED_FORWARD_APPROVED_APPOINTMENT_PURPOSE
            ).map(LicenceBlockSubareaDto::displayName)
            .orElse(preSelectedAppointment.getAsset().getAssetName());

        modelAndView.addObject("preSelectedForwardApprovedAppointment", Map.of(
            preSelectedAppointment.getId(),
            ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING.formatted(subareaName, startDate)
        ));
      });
    }

    if (PortalAssetType.SUBAREA.equals(assetDto.portalAssetType())) {
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
