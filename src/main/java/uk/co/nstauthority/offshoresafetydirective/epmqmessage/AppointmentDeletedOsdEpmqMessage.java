package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

import java.time.Instant;
import java.util.UUID;

public class AppointmentDeletedOsdEpmqMessage extends OsdEpmqMessage {

  public static final String TYPE = "APPOINTMENT_DELETED";

  private UUID appointmentId;

  public AppointmentDeletedOsdEpmqMessage() {
    super(TYPE, null, null);
  }

  public AppointmentDeletedOsdEpmqMessage(UUID appointmentId,
                                          String correlationId,
                                          Instant createdInstant) {
    super(TYPE, correlationId, createdInstant);
    this.appointmentId = appointmentId;
  }

  public UUID getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(UUID appointmentId) {
    this.appointmentId = appointmentId;
  }
}
