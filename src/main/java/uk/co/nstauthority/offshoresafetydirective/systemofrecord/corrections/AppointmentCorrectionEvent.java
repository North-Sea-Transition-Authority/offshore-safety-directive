package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;


import org.springframework.context.ApplicationEvent;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;

public class AppointmentCorrectionEvent extends ApplicationEvent {

  public AppointmentCorrectionEvent(AppointmentId source) {
    super(source);
  }

  public AppointmentId getAppointment() {
    return (AppointmentId) getSource();
  }
}
