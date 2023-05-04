package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import org.springframework.context.ApplicationEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

public class NominationUpdateRequestedEvent extends ApplicationEvent {

  private final NominationId nominationId;

  public NominationUpdateRequestedEvent(NominationId nominationId) {
    super(nominationId);
    this.nominationId = nominationId;
  }

  public NominationId getNominationId() {
    return nominationId;
  }

}
