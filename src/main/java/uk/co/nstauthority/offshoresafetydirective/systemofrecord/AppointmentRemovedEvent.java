package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import org.springframework.context.ApplicationEvent;

public class AppointmentRemovedEvent extends ApplicationEvent {

  public AppointmentRemovedEvent(AppointmentId source) {
    super(source);
  }

  public AppointmentId getAppointment() {
    return (AppointmentId) getSource();
  }
}
