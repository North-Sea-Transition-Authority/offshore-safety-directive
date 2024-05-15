package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import java.io.Serial;
import org.springframework.context.ApplicationEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

public class NominationDecisionDeterminedEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = -1259317413324195372L;

  private final NominationId nominationId;

  public NominationDecisionDeterminedEvent(NominationId nominationId) {
    super(nominationId);
    this.nominationId = nominationId;
  }

  public NominationId getNominationId() {
    return nominationId;
  }
}
