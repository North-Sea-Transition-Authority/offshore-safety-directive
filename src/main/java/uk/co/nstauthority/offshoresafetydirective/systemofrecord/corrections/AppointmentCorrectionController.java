package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedPortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AppointmentTimelineController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/appointment/{appointmentId}/correct")
@HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
public class AppointmentCorrectionController {

  private final AppointmentAccessService appointmentAccessService;
  private final AssetAccessService assetAccessService;
  private final PortalAssetNameService portalAssetNameService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final AppointmentCorrectionService appointmentCorrectionService;
  private final AppointmentCorrectionValidator appointmentCorrectionValidator;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public AppointmentCorrectionController(AppointmentAccessService appointmentAccessService,
                                         AssetAccessService assetAccessService,
                                         PortalAssetNameService portalAssetNameService,
                                         PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                         AppointmentCorrectionService appointmentCorrectionService,
                                         AppointmentCorrectionValidator appointmentCorrectionValidator,
                                         ControllerHelperService controllerHelperService) {
    this.appointmentAccessService = appointmentAccessService;
    this.assetAccessService = assetAccessService;
    this.portalAssetNameService = portalAssetNameService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.appointmentCorrectionService = appointmentCorrectionService;
    this.appointmentCorrectionValidator = appointmentCorrectionValidator;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView renderCorrection(@PathVariable AppointmentId appointmentId) {

    var appointmentDto = getAppointmentDto(appointmentId);
    var assetDto = getAssetDto(appointmentDto.portalAssetId());
    var form = appointmentCorrectionService.getForm(appointmentDto);

    return getModelAndView(appointmentDto, assetDto, form);
  }

  @PostMapping
  public ModelAndView submitCorrection(@PathVariable AppointmentId appointmentId,
                                       @Nullable @ModelAttribute("form") AppointmentCorrectionForm form,
                                       @Nullable BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {

    var appointmentDto = getAppointmentDto(appointmentId);
    var assetDto = getAssetDto(appointmentDto.portalAssetId());

    appointmentCorrectionValidator.validate(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(
        Objects.requireNonNull(bindingResult),
        getModelAndView(appointmentDto, assetDto, form),
        form,
        () -> {
          appointmentCorrectionService.updateCorrection(appointmentDto, Objects.requireNonNull(form));

          var assetName = getAssetName(assetDto);
          var notificationBanner = NotificationBanner.builder()
              .withBannerType(NotificationBannerType.SUCCESS)
              .withHeading("Corrected appointment for %s".formatted(assetName.value()))
              .build();

          NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);

          return getSubmitRedirectRoute(appointmentDto, assetDto);
        });
  }

  private ModelAndView getSubmitRedirectRoute(AppointmentDto appointmentDto, AssetDto assetDto) {
    return switch (assetDto.portalAssetType()) {
      case INSTALLATION -> ReverseRouter.redirect(on(AppointmentTimelineController.class)
          .renderInstallationAppointmentTimeline(appointmentDto.portalAssetId().toPortalAssetId()));
      case WELLBORE -> ReverseRouter.redirect(on(AppointmentTimelineController.class)
          .renderWellboreAppointmentTimeline(appointmentDto.portalAssetId().toPortalAssetId()));
      case SUBAREA -> ReverseRouter.redirect(on(AppointmentTimelineController.class)
          .renderSubareaAppointmentTimeline(appointmentDto.portalAssetId().toPortalAssetId()));
    };
  }

  private ModelAndView getModelAndView(AppointmentDto appointmentDto, AssetDto assetDto,
                                       AppointmentCorrectionForm form) {

    var assetName = getAssetName(assetDto);

    return new ModelAndView("osd/systemofrecord/correction/correctAppointment")
        .addObject("assetName", assetName.value())
        .addObject("assetTypeDisplayName", assetDto.portalAssetType().getDisplayName())
        .addObject("submitUrl", ReverseRouter.route(on(AppointmentCorrectionController.class)
            .submitCorrection(appointmentDto.appointmentId(), null, null, null)))
        .addObject("portalOrganisationsRestUrl", RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
            .searchAllPortalOrganisations(null)))
        .addObject("preselectedOperator", getPreselectedOperator(form))
        .addObject("form", form);
  }

  private AssetName getAssetName(AssetDto assetDto) {
    return portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType())
        .orElse(assetDto.assetName());
  }

  private Map<String, String> getPreselectedOperator(AppointmentCorrectionForm form) {
    var operator = portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId());
    return operator.stream()
        .collect(Collectors.toMap(
            portalOrganisationDto -> String.valueOf(portalOrganisationDto.id()),
            OrganisationUnitDisplayUtil::getOrganisationUnitDisplayName
        ));
  }

  private AppointmentDto getAppointmentDto(AppointmentId appointmentId) {
    return appointmentAccessService.findAppointmentDtoById(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Appointment with ID [%s] could not be found".formatted(
                appointmentId.id()
            )
        ));
  }

  private AssetDto getAssetDto(AppointedPortalAssetId appointedPortalAssetId) {
    return assetAccessService.getAsset(appointedPortalAssetId.toPortalAssetId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Asset with ID [%s] could not be found".formatted(
                appointedPortalAssetId.toPortalAssetId().id()
            )
        ));
  }


}
