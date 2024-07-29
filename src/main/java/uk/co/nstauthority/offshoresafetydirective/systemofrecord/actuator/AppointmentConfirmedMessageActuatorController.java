package uk.co.nstauthority.offshoresafetydirective.systemofrecord.actuator;

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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentSnsService;

@Component
@WebEndpoint(id = "appointment-confirmed-message")
@Profile("!disable-epmq")
public class AppointmentConfirmedMessageActuatorController {

  private final AppointmentAccessService appointmentAccessService;

  private final AppointmentSnsService appointmentSnsService;

  @Autowired
  public AppointmentConfirmedMessageActuatorController(AppointmentAccessService appointmentAccessService,
                                                       AppointmentSnsService appointmentSnsService) {
    this.appointmentAccessService = appointmentAccessService;
    this.appointmentSnsService = appointmentSnsService;
  }

  @WriteOperation
  ResponseEntity<Void> publishMessage(@Selector UUID appointmentId) {
    var appointment = appointmentAccessService.getAppointment(new AppointmentId(appointmentId))
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot find appointment %s".formatted(appointmentId)
        ));
    appointmentSnsService.publishAppointmentCreatedSnsMessage(appointment);
    return ResponseEntity.ok().build();
  }
}
