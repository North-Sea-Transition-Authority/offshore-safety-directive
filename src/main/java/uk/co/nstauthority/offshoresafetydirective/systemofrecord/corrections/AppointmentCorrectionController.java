package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
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
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasStaticRole;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentModelAndViewService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation.HasAssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation.HasNotBeenTerminated;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Controller
@RequestMapping("/appointment/{appointmentId}/correct")
@InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.APPOINTMENT_MANAGER)
@HasNotBeenTerminated
@HasAssetStatus(AssetStatus.EXTANT)
public class AppointmentCorrectionController {

  private final AppointmentAccessService appointmentAccessService;
  private final AppointmentModelAndViewService appointmentModelAndViewService;
  private final PortalAssetNameService portalAssetNameService;
  private final AppointmentCorrectionService appointmentCorrectionService;
  private final AppointmentCorrectionValidator appointmentCorrectionValidator;

  @Autowired
  public AppointmentCorrectionController(AppointmentAccessService appointmentAccessService,
                                         AppointmentModelAndViewService appointmentModelAndViewService,
                                         PortalAssetNameService portalAssetNameService,
                                         AppointmentCorrectionService appointmentCorrectionService,
                                         AppointmentCorrectionValidator appointmentCorrectionValidator) {
    this.appointmentAccessService = appointmentAccessService;
    this.appointmentModelAndViewService = appointmentModelAndViewService;
    this.portalAssetNameService = portalAssetNameService;
    this.appointmentCorrectionService = appointmentCorrectionService;
    this.appointmentCorrectionValidator = appointmentCorrectionValidator;
  }

  @GetMapping
  public ModelAndView renderCorrection(@PathVariable AppointmentId appointmentId) {

    var appointment = getAppointment(appointmentId);
    var form = appointmentCorrectionService.getForm(appointment);

    return getModelAndView(appointment, form);
  }

  @PostMapping
  public ModelAndView submitCorrection(@PathVariable AppointmentId appointmentId,
                                       @Nullable @ModelAttribute("form") AppointmentCorrectionForm form,
                                       @Nullable BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {

    var appointment = getAppointment(appointmentId);
    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    var validatorHint = new AppointmentCorrectionValidationHint(
        appointmentId,
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionValidator.validate(form, bindingResult, validatorHint);

    if (Objects.requireNonNull(bindingResult).hasErrors()) {
      return getModelAndView(appointment, form);
    }

    appointmentCorrectionService.correctAppointment(
        appointment,
        Objects.requireNonNull(form)
    );

    var assetName = getAssetName(appointmentDto.assetDto());
    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Updated appointment for %s".formatted(assetName.value()))
        .build();

    NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);

    return getSubmitRedirectRoute(appointmentDto);
  }

  private ModelAndView getSubmitRedirectRoute(AppointmentDto appointmentDto) {
    return switch (appointmentDto.assetDto().portalAssetType()) {
      case INSTALLATION -> ReverseRouter.redirect(on(AssetTimelineController.class)
          .renderInstallationTimeline(appointmentDto.assetDto().portalAssetId()));
      case WELLBORE -> ReverseRouter.redirect(on(AssetTimelineController.class)
          .renderWellboreTimeline(appointmentDto.assetDto().portalAssetId()));
      case SUBAREA -> ReverseRouter.redirect(on(AssetTimelineController.class)
          .renderSubareaTimeline(appointmentDto.assetDto().portalAssetId()));
    };
  }

  private ModelAndView getModelAndView(Appointment appointment,
                                       AppointmentCorrectionForm form) {

    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    var assetDto = appointmentDto.assetDto();
    var correctionHistoryViews = appointmentCorrectionService.getAppointmentCorrectionHistoryViews(appointment);

    return appointmentModelAndViewService.getAppointmentModelAndView(
        "Update appointment",
        assetDto,
        form,
        ReverseRouter.route(on(AppointmentCorrectionController.class)
            .submitCorrection(appointmentDto.appointmentId(), null, null, null)))
        .addObject("correctionHistoryViews", correctionHistoryViews)
        .addObject("showCorrectionHistory", true);
  }

  private AssetName getAssetName(AssetDto assetDto) {
    return portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType())
        .map(AssetName::new)
        .orElse(assetDto.assetName());
  }

  private Appointment getAppointment(AppointmentId appointmentId) {
    return appointmentAccessService.getAppointment(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Appointment with ID [%s] could not be found".formatted(
                appointmentId.id()
            )
        ));
  }
}
