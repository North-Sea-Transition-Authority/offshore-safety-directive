package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.wellbore;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.streamutil.StreamUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
public class WellboreAppointmentRestService {

  static final RequestPurpose WELL_SEARCH_PURPOSE = new RequestPurpose("Get wellbores by registration number");
  private final WellQueryService wellQueryService;
  private final AppointmentAccessService appointmentAccessService;

  @Autowired
  public WellboreAppointmentRestService(WellQueryService wellQueryService, AppointmentAccessService appointmentAccessService) {
    this.wellQueryService = wellQueryService;
    this.appointmentAccessService = appointmentAccessService;
  }

  public List<RestSearchItem> searchWellboreAppointments(String searchTerm) {
    Map<String, WellDto> wellIdAndWellDtoMap = wellQueryService.searchWellsByRegistrationNumber(searchTerm, WELL_SEARCH_PURPOSE)
        .stream()
        .collect(StreamUtil.toLinkedHashMap(
            wellDto -> String.valueOf(wellDto.wellboreId().id()),
            Function.identity()
        ));

    var wellIds = wellIdAndWellDtoMap.values()
        .stream()
        .map(wellDto -> String.valueOf(wellDto.wellboreId().id()))
        .toList();
    var appointments = appointmentAccessService.getAppointmentsForAssets(
        EnumSet.of(AppointmentStatus.EXTANT, AppointmentStatus.TERMINATED),
        wellIds,
        PortalAssetType.WELLBORE
    );

    return wellIds.stream()
        .flatMap(wellId -> appointments.stream()
            .filter(appointment -> appointment.getAsset().getPortalAssetId().equals(wellId))
            .sorted(Comparator.comparing(Appointment::getResponsibleFromDate).reversed())
        )
        .map(appointment -> getRestSearchItem(
            appointment,
            wellIdAndWellDtoMap.get(appointment.getAsset().getPortalAssetId()).name()
        ))
        .toList();
  }

  private RestSearchItem getRestSearchItem(Appointment appointment, String wellName) {
    var displayName = WellboreAppointmentRestService.formatSearchItemName(
        wellName,
        appointment.getResponsibleFromDate()
    );
    return new RestSearchItem(
        appointment.getId().toString(),
        displayName
    );
  }

  public static String formatSearchItemName(String wellName, LocalDate fromDate) {
    return "%s on %s".formatted(
        wellName,
        DateUtil.formatLongDate(fromDate)
    );
  }

}
