package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasAssetStatus;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentModelAndViewService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionForm;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionValidationHint;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionValidator;

@Controller
@RequestMapping
// TODO OSDOP-811 @HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
public class NewAppointmentController {

  private final PortalAssetRetrievalService portalAssetRetrievalService;
  private final AppointmentService appointmentService;
  private final AssetPersistenceService assetPersistenceService;
  private final AppointmentCorrectionValidator appointmentCorrectionValidator;
  private final AssetAccessService assetAccessService;
  private final AppointmentModelAndViewService appointmentModelAndViewService;

  @Autowired
  public NewAppointmentController(PortalAssetRetrievalService portalAssetRetrievalService,
                                  AppointmentService appointmentService,
                                  AssetPersistenceService assetPersistenceService,
                                  AppointmentCorrectionValidator appointmentCorrectionValidator,
                                  AssetAccessService assetAccessService,
                                  AppointmentModelAndViewService appointmentModelAndViewService) {
    this.portalAssetRetrievalService = portalAssetRetrievalService;
    this.appointmentService = appointmentService;
    this.assetPersistenceService = assetPersistenceService;
    this.appointmentCorrectionValidator = appointmentCorrectionValidator;
    this.assetAccessService = assetAccessService;
    this.appointmentModelAndViewService = appointmentModelAndViewService;
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
      return ReverseRouter.redirect(on(NewAppointmentController.class)
          .renderNewAppointment(AssetId.fromAsset(asset)));
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

    if (bindingResult.hasErrors()) {
      return getNewAppointmentForm(assetDto, form);
    }

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

  private ModelAndView getNewAppointmentForm(AssetDto assetDto, AppointmentCorrectionForm form) {

    return appointmentModelAndViewService.getAppointmentModelAndView(
        "Add appointment",
        assetDto,
        form,
        ReverseRouter.route(on(NewAppointmentController.class)
            .createNewAppointment(assetDto.assetId(), null, null, null)));
  }
}
