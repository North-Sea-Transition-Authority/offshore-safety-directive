package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasStaticRole;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.SubareaDto;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@RestController
@RequestMapping("/api/appointments/forward-approvals")
@InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.APPOINTMENT_MANAGER)
public class ForwardApprovedAppointmentRestController {

  public static final String SEARCH_DISPLAY_STRING = "%s: %s";
  static final RequestPurpose FORWARD_APPROVED_SEARCH_PURPOSE =
      new RequestPurpose("Forward approved appointments search selector (search forward approved appointments)");
  static final EnumSet<AppointmentStatus> STATUSES = EnumSet.of(AppointmentStatus.EXTANT, AppointmentStatus.TERMINATED);
  private final AppointmentAccessService appointmentAccessService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  public ForwardApprovedAppointmentRestController(AppointmentAccessService appointmentAccessService,
                                                  LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.appointmentAccessService = appointmentAccessService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  @GetMapping
  public RestSearchResult searchSubareaAppointments(@RequestParam("term") String searchTerm) {
    List<LicenceBlockSubareaDto> portalSubareas = licenceBlockSubareaQueryService
        .searchSubareasByDisplayName(searchTerm, FORWARD_APPROVED_SEARCH_PURPOSE)
        .stream()
        .sorted(LicenceBlockSubareaDto.sort())
        .toList();

    if (portalSubareas.isEmpty()) {
      return new RestSearchResult(List.of());
    }

    var portalSubareaIds = portalSubareas.stream()
        .map(SubareaDto::subareaId)
        .map(LicenceBlockSubareaId::id)
        .toList();

    var subareaAppointments = appointmentAccessService.getAppointmentsForAssets(
        STATUSES,
        portalSubareaIds,
        PortalAssetType.SUBAREA
    );

    Map<String, List<Appointment>> portalAssetIdToAppointmentsMap = subareaAppointments.stream()
        .collect(Collectors.groupingBy(appointment -> appointment.getAsset().getPortalAssetId()));

    var results = new ArrayList<RestSearchItem>();

    portalSubareas.forEach(licenceBlockSubareaDto -> {

      List<Appointment> appointmentsForSubarea = portalAssetIdToAppointmentsMap.get(licenceBlockSubareaDto.subareaId().id());

      if (appointmentsForSubarea != null) {
        appointmentsForSubarea.stream()
            .sorted(Comparator.comparing(Appointment::getResponsibleFromDate))
            .forEach(appointment -> {
              var startDate = DateUtil.formatLongDate(appointment.getResponsibleFromDate());
              var subareaName = licenceBlockSubareaDto.displayName();

              results.add(
                  new RestSearchItem(
                      appointment.getId().toString(),
                      SEARCH_DISPLAY_STRING.formatted(subareaName, startDate)
                  )
              );
            });
      }
    });

    return new RestSearchResult(results);
  }
}
