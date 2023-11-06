package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;

@Component
@RestControllerEndpoint(id = "appointments")
@Profile("!disable-epmq")
class AppointmentActuatorController {

  private final AppointmentAccessService appointmentAccessService;
  private final AppointmentSnsService appointmentSnsService;

  @Autowired
  AppointmentActuatorController(AppointmentAccessService appointmentAccessService,
                                AppointmentSnsService appointmentSnsService) {
    this.appointmentAccessService = appointmentAccessService;
    this.appointmentSnsService = appointmentSnsService;
  }

  @PostMapping("publish-epmq-message/appointment/{appointmentId}/confirmed")
  ResponseEntity<Void> publishAppointmentCreatedMessage(@PathVariable("appointmentId") AppointmentId appointmentId) {
    var appointment = appointmentAccessService.getAppointment(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot find appointment %s".formatted(appointmentId.id())));
    appointmentSnsService.publishAppointmentCreatedSnsMessage(appointment);
    return ResponseEntity.ok().build();
  }

  @PostMapping("publish-epmq-message/appointment/{appointmentId}/deleted")
  ResponseEntity<Void> publishAppointmentDeletedMessage(@PathVariable("appointmentId") AppointmentId appointmentId) {
    var optionalAppointment = appointmentAccessService.getAppointment(appointmentId);

    var isPresentAndTerminatedOrRemoved = optionalAppointment.isPresent()
        && (AppointmentStatus.TERMINATED.equals(optionalAppointment.get().getAppointmentStatus())
        || AppointmentStatus.REMOVED.equals(optionalAppointment.get().getAppointmentStatus()));

    var isPresentAndExtantWithEndDate =  optionalAppointment.isPresent()
        && (AppointmentStatus.EXTANT.equals(optionalAppointment.get().getAppointmentStatus())
        && optionalAppointment.get().getResponsibleToDate() != null);

    if (isPresentAndTerminatedOrRemoved || isPresentAndExtantWithEndDate) {
      var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();
      appointmentSnsService.publishAppointmentDeletedSnsMessage(appointmentId.id(), correlationId);
      return ResponseEntity.ok().build();
    }

    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Appointment with ID [%s] was expected to be deleted but still exists".formatted(appointmentId.id()));
  }

  @PostMapping("publish-epmq-message/appointment/{appointmentId}/updated")
  ResponseEntity<Void> publishAppointmentUpdatedMessage(@PathVariable("appointmentId") AppointmentId appointmentId) {

    var appointment = appointmentAccessService.getAppointmentByStatus(appointmentId, AppointmentStatus.EXTANT)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot find appointment %s".formatted(appointmentId.id())));

    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    if (appointment.getResponsibleToDate() == null) {
      appointmentSnsService.publishAppointmentUpdatedSnsMessage(appointment, correlationId);
    } else {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Appointment %s is not current so cannot be updated".formatted(appointmentId.id()));
    }

    return ResponseEntity.ok().build();
  }
}
