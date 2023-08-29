package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTermination;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationService;

@Service
class TerminationTimelineItemService {

  private final EnergyPortalUserService energyPortalUserService;
  private final AppointmentTerminationService appointmentTerminationService;

  @Autowired
  TerminationTimelineItemService(EnergyPortalUserService energyPortalUserService,
                                 AppointmentTerminationService appointmentTerminationService) {
    this.energyPortalUserService = energyPortalUserService;
    this.appointmentTerminationService = appointmentTerminationService;
  }

  public List<AssetTimelineItemView> getTimelineItemViews(List<Appointment> appointments) {

    var terminations = appointmentTerminationService.getTerminations(appointments);

    var wuaIds = terminations.stream()
        .map(AppointmentTermination::getTerminatedByWuaId)
        .map(WebUserAccountId::new)
        .collect(Collectors.toSet());

    Map<Long, EnergyPortalUserDto> users = energyPortalUserService.findByWuaIds(wuaIds)
        .stream()
        .collect(Collectors.toMap(EnergyPortalUserDto::webUserAccountId, Function.identity()));

    return terminations.stream()
        .map(termination -> {
          var terminatedByUserName = Optional.ofNullable(users.get(termination.getTerminatedByWuaId()))
              .map(EnergyPortalUserDto::displayName)
              .orElse("Unknown");

          return convertToTimelineItemView(termination, terminatedByUserName);
        })
        .toList();

  }

  private AssetTimelineItemView convertToTimelineItemView(AppointmentTermination termination,
                                                          String terminatedByUserName) {

    var modelProperties = new AssetTimelineModelProperties()
        .addProperty("terminationDate", DateUtil.formatLongDate(termination.getTerminationDate()))
        .addProperty("reasonForTermination", termination.getReasonForTermination())
        .addProperty("terminatedBy", terminatedByUserName);

    return new AssetTimelineItemView(
        TimelineEventType.TERMINATION,
        "Termination of appointment",
        modelProperties,
        termination.getCreatedTimestamp(),
        termination.getTerminationDate()
    );
  }
}
