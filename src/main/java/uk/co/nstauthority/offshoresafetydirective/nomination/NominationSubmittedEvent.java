package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.io.Serial;
import org.springframework.context.ApplicationEvent;

public class NominationSubmittedEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = 1694652841778510682L;
  private final NominationId nominationId;

  public NominationSubmittedEvent(NominationId nominationId) {
    super(nominationId);
    this.nominationId = nominationId;
  }

  public NominationId getNominationId() {
    return nominationId;
  }
}
