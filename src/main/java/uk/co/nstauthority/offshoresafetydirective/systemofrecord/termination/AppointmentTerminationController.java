package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

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
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasStaticRole;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.UnlinkedFileController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation.HasAssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation.HasNotBeenTerminated;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation.IsCurrentAppointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineController;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Controller
@RequestMapping("/appointment/{appointmentId}/termination")
@InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.APPOINTMENT_MANAGER)
@IsCurrentAppointment
@HasNotBeenTerminated
@HasAssetStatus(AssetStatus.EXTANT)
public class AppointmentTerminationController {

  private final AppointmentTerminationService appointmentTerminationService;
  private final AppointmentTerminationValidator appointmentTerminationValidator;
  private final FileService fileService;

  @Autowired
  public AppointmentTerminationController(AppointmentTerminationService appointmentTerminationService,
                                          AppointmentTerminationValidator appointmentTerminationValidator,
                                          FileService fileService) {
    this.appointmentTerminationService = appointmentTerminationService;
    this.appointmentTerminationValidator = appointmentTerminationValidator;
    this.fileService = fileService;
  }

  @GetMapping
  public ModelAndView renderTermination(@PathVariable AppointmentId appointmentId) {
    var appointment = appointmentTerminationService.getAppointment(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Appointment with ID [%s] could not be found".formatted(
                appointmentId.id()
            )
        ));
    return getModelAndView(appointment, new AppointmentTerminationForm());
  }

  @PostMapping
  public ModelAndView submitTermination(@PathVariable AppointmentId appointmentId,
                                        @Nullable @ModelAttribute("form") AppointmentTerminationForm form,
                                        @Nullable BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {
    var appointment = appointmentTerminationService.getAppointment(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Appointment with ID [%s] could not be found".formatted(
                appointmentId.id()
            )
        ));

    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    var validatorHint = new AppointmentTerminationValidatorHint(appointmentDto);

    appointmentTerminationValidator.validate(
        Objects.requireNonNull(form),
        Objects.requireNonNull(bindingResult),
        validatorHint
    );

    if (bindingResult.hasErrors()) {
      return getModelAndView(appointment, form);
    }

    appointmentTerminationService.terminateAppointment(appointment, form);
    var assetName = appointmentTerminationService.getAssetName(appointmentDto.assetDto());

    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Terminated appointment for %s".formatted(assetName.value()))
        .build();

    NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);
    return getSubmitRedirectRoute(appointmentDto);

  }

  private ModelAndView getModelAndView(Appointment appointment, AppointmentTerminationForm form) {
    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    var assetDto = appointmentDto.assetDto();
    var assetName = appointmentTerminationService.getAssetName(assetDto);

    return new ModelAndView("osd/systemofrecord/termination/terminateAppointment")
        .addObject("assetName", assetName.value())
        .addObject("appointedOperator",
            appointmentTerminationService.getAppointedOperator(appointmentDto.appointedOperatorId()))
        .addObject("responsibleFromDate", DateUtil.formatLongDate(appointmentDto.appointmentFromDate().value()))
        .addObject("phases", appointmentTerminationService.getAppointmentPhases(appointment, assetDto))
        .addObject("createdBy", appointmentTerminationService.getCreatedByDisplayString(appointmentDto))
        .addObject("form", form)
        .addObject("submitUrl",
            ReverseRouter.route(on(AppointmentTerminationController.class)
                .submitTermination(new AppointmentId(appointment.getId()), null, null, null)))
        .addObject("timelineUrl", getTimelineRoute(appointmentDto))
        .addObject("fileUploadTemplate", buildFileUploadComponentAttributes())
        .addObject("uploadedFiles", Objects.requireNonNull(form).getTerminationDocuments());
  }

  ModelAndView getSubmitRedirectRoute(AppointmentDto appointmentDto) {
    return switch (appointmentDto.assetDto().portalAssetType()) {
      case WELLBORE -> ReverseRouter.redirect(on(AssetTimelineController.class)
          .renderWellboreTimeline(appointmentDto.assetDto().portalAssetId()));
      case INSTALLATION -> ReverseRouter.redirect(on(AssetTimelineController.class)
          .renderInstallationTimeline(appointmentDto.assetDto().portalAssetId()));
      case SUBAREA -> ReverseRouter.redirect(on(AssetTimelineController.class)
          .renderSubareaTimeline(appointmentDto.assetDto().portalAssetId()));
    };
  }

  String getTimelineRoute(AppointmentDto appointmentDto) {
    return switch (appointmentDto.assetDto().portalAssetType()) {
      case WELLBORE -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderWellboreTimeline(appointmentDto.assetDto().portalAssetId()));
      case INSTALLATION -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderInstallationTimeline(appointmentDto.assetDto().portalAssetId()));
      case SUBAREA -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderSubareaTimeline(appointmentDto.assetDto().portalAssetId()));
    };
  }

  private FileUploadComponentAttributes buildFileUploadComponentAttributes() {
    return fileService.getFileUploadAttributes()
        .withDownloadUrl(ReverseRouter.route(on(UnlinkedFileController.class).download(null)))
        .withDeleteUrl(ReverseRouter.route(on(UnlinkedFileController.class).delete(null)))
        .withUploadUrl(
            ReverseRouter.route(
                on(UnlinkedFileController.class).upload(null, FileDocumentType.TERMINATION.name()))
        )
        .build();
  }
}
