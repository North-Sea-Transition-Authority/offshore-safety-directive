package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.UUID;

public record AppointmentId(UUID id) {

  // Required so AppointmentId can be used as an @PathVariable in controllers.
  public static AppointmentId valueOf(String value) {
    return new AppointmentId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
