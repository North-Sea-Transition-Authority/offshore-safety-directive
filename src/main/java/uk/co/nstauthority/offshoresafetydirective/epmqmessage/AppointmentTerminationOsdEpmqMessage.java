package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

import java.time.Instant;
import java.util.UUID;

public class AppointmentTerminationOsdEpmqMessage extends OsdEpmqMessage {

  public static final String TYPE = "APPOINTMENT_TERMINATION";

  private UUID appointmentId;

  public AppointmentTerminationOsdEpmqMessage() {
    super(TYPE, null, null);
  }

  public AppointmentTerminationOsdEpmqMessage(UUID appointmentId,
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
