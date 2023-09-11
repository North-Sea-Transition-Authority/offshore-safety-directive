package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import org.springframework.context.ApplicationEvent;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;

public class AppointmentTerminationEvent extends ApplicationEvent {

  public AppointmentTerminationEvent(AppointmentId source) {
    super(source);
  }

  public AppointmentId getAppointmentId() {
    return (AppointmentId) getSource();
  }
}
