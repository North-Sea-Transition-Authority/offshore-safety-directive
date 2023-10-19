package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasAppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasAssetStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentPhasesService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/appointment/{appointmentId}/remove")
@HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
@HasAppointmentStatus(AppointmentStatus.EXTANT)
@HasAssetStatus(AssetStatus.EXTANT)
class RemoveAppointmentController {

  private final AppointmentAccessService appointmentAccessService;
  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private final AppointmentPhasesService appointmentPhasesService;
  private final AppointmentTimelineItemService appointmentTimelineItemService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final AppointmentService appointmentService;

  @Autowired
  RemoveAppointmentController(AppointmentAccessService appointmentAccessService,
                              AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                              AppointmentPhasesService appointmentPhasesService,
                              AppointmentTimelineItemService appointmentTimelineItemService,
                              PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                              AppointmentService appointmentService) {
    this.appointmentAccessService = appointmentAccessService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.appointmentPhasesService = appointmentPhasesService;
    this.appointmentTimelineItemService = appointmentTimelineItemService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.appointmentService = appointmentService;
  }

  @GetMapping
  public ModelAndView renderRemoveAppointment(@PathVariable AppointmentId appointmentId) {
    var appointment = appointmentAccessService.getAppointment(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No Appointment found with ID [%s]".formatted(
                appointmentId.id()
            )
        ));

    var appointmentDto = AppointmentDto.fromAppointment(appointment);

    var timelineItem = appointmentTimelineItemService.getTimelineItemViews(
            List.of(appointment),
            appointmentDto.assetDto()
        )
        .stream()
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "No AppointmentTimelineItemView for Appointment with ID [%s]"
        ));

    var operatorName = Optional.of(appointmentDto.appointedOperatorId())
        .map(AppointedOperatorId::id)
        .map(Integer::parseInt)
        .flatMap(portalOrganisationUnitQueryService::getOrganisationById)
        .map(PortalOrganisationDto::displayName)
        .orElse("Unknown operator");

    var phases = assetAppointmentPhaseAccessService.getPhasesByAppointment(appointment);
    var displayPhases = appointmentPhasesService.getDisplayTextAppointmentPhases(
        appointmentDto.assetDto(),
        phases
    );

    return new ModelAndView("osd/systemofrecord/timeline/removeAppointment")
        .addObject(
            "pageTitle",
            "Are you sure you want to remove this appointment for %s?".formatted(
                appointmentDto.assetDto().assetName().value()
            ))
        .addObject("operatorName", operatorName)
        .addObject("displayPhases", displayPhases)
        .addObject("timelineItemView", timelineItem)
        .addObject("assetName", appointmentDto.assetDto().assetName().value())
        .addObject("cancelUrl", AssetTimelineController.determineRouteByPortalAssetType(
            appointmentDto.assetDto().portalAssetId(),
            appointmentDto.assetDto().portalAssetType()
        ));
  }

  @PostMapping
  public ModelAndView removeAppointment(@PathVariable AppointmentId appointmentId,
                                        RedirectAttributes redirectAttributes) {
    var appointment = appointmentAccessService.getAppointment(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No Appointment found with ID [%s]".formatted(
                appointmentId.id()
            )
        ));

    var appointmentDto = AppointmentDto.fromAppointment(appointment);

    appointmentService.removeAppointment(appointment);

    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Removed appointment for %s".formatted(
            appointmentDto.assetDto().assetName().value()
        ))
        .build();

    NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);

    return AssetTimelineController.determineRedirectByPortalAssetType(
        appointmentDto.assetDto().portalAssetId(),
        appointmentDto.assetDto().portalAssetType()
    );
  }

}
