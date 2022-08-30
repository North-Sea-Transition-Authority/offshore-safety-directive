package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.springframework.context.ApplicationEvent;

public class NominationSubmittedEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1694652841778510682L;
  private final NominationDetail nominationDetail;

  NominationSubmittedEvent(Object source, NominationDetail nominationDetail) {
    super(source);
    this.nominationDetail = nominationDetail;
  }

  public NominationDetail getNominationDetail() {
    return nominationDetail;
  }
}
