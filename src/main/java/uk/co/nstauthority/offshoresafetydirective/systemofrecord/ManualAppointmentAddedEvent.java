package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import org.springframework.context.ApplicationEvent;

public class ManualAppointmentAddedEvent extends ApplicationEvent {

  public ManualAppointmentAddedEvent(AppointmentId source) {
    super(source);
  }

  public AppointmentId getAppointment() {
    return (AppointmentId) getSource();
  }
}
