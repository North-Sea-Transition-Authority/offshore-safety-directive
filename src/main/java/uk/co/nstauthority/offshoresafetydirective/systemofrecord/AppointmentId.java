package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record AppointmentId(UUID id) {

  // Required so AppointmentId can be used as an @PathVariable in controllers.
  public static AppointmentId valueOf(String value) {
    try {
      return new AppointmentId(UUID.fromString(value));
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          String.format("Cannot find Appointment with ID: %s", value)
      );
    }
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
