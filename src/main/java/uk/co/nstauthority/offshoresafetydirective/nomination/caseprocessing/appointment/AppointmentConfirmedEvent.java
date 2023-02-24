package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import org.springframework.context.ApplicationEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

public class AppointmentConfirmedEvent extends ApplicationEvent {

  public AppointmentConfirmedEvent(NominationId source) {
    super(source);
  }

  public NominationId getNominationId() {
    return (NominationId) getSource();
  }
}
