package uk.co.nstauthority.offshoresafetydirective.systemofrecord.actuator;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentSnsService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;

@Component
@WebEndpoint(id = "appointment-deleted-message")
@Profile("!disable-epmq")
public class AppointmentDeletedMessageActuatorController {

  private final AppointmentAccessService appointmentAccessService;

  private final AppointmentSnsService appointmentSnsService;

  @Autowired
  public AppointmentDeletedMessageActuatorController(AppointmentAccessService appointmentAccessService,
                                                     AppointmentSnsService appointmentSnsService) {
    this.appointmentAccessService = appointmentAccessService;
    this.appointmentSnsService = appointmentSnsService;
  }

  @WriteOperation
  ResponseEntity<Void> publishMessage(@Selector UUID appointmentId) {

    Optional<Appointment> appointment = appointmentAccessService.getAppointment(new AppointmentId(appointmentId));

    if (appointment.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "No appointment with ID %s found".formatted(appointmentId)
      );
    } else if (AppointmentStatus.EXTANT.equals(appointment.get().getAppointmentStatus())
        && appointment.get().getResponsibleToDate() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Appointment with ID %s is still active".formatted(appointmentId)
      );
    }

    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();
    appointmentSnsService.publishAppointmentDeletedSnsMessage(appointmentId, correlationId);

    return ResponseEntity.ok().build();
  }
}
