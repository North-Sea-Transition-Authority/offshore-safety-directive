package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;

@Component
@RestControllerEndpoint(id = "appointments")
@Profile("!disable-epmq")
class AppointmentActuatorController {

  private final AppointmentAccessService appointmentAccessService;
  private final AppointmentSnsService appointmentSnsService;

  @Autowired
  AppointmentActuatorController(
      AppointmentAccessService appointmentAccessService,
      AppointmentSnsService appointmentSnsService
  ) {
    this.appointmentAccessService = appointmentAccessService;
    this.appointmentSnsService = appointmentSnsService;
  }

  @PostMapping("publish-epmq-message/appointment/{appointmentId}")
  ResponseEntity<Void> publishAppointmentConfirmedMessage(@PathVariable("appointmentId") AppointmentId appointmentId) {
    var appointment = appointmentAccessService.getAppointment(appointmentId)
        .orElseThrow(() -> new OsdEntityNotFoundException("Cannot find appointment %s".formatted(appointmentId.id())));
    appointmentSnsService.publishAppointmentCreatedSnsMessage(appointment);
    return ResponseEntity.ok().build();
  }
}
