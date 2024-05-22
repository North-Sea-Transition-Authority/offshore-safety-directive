package uk.co.nstauthority.offshoresafetydirective.systemofrecord.message.ended;

import java.io.Serial;
import java.util.UUID;
import org.springframework.context.ApplicationEvent;

public class AppointmentEndedEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = -6763709721415699073L;

  public AppointmentEndedEvent(UUID appointmentId) {
    super(appointmentId);
  }

  public UUID getAppointmentId() {
    return (UUID) source;
  }
}
