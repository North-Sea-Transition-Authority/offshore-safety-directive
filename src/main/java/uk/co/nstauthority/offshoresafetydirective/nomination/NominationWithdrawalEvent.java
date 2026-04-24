package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.io.Serial;
import org.springframework.context.ApplicationEvent;

public class NominationWithdrawalEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = 4783947643698950710L;

  private final NominationId nominationId;

  public NominationWithdrawalEvent(NominationId nominationId) {
    super(nominationId);
    this.nominationId = nominationId;
  }

  public NominationId getNominationId() {
    return nominationId;
  }
}
