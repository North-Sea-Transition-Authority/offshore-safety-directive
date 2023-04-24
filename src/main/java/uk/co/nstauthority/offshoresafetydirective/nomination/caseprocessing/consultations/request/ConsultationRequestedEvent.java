package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request;

import java.io.Serial;
import org.springframework.context.ApplicationEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

public class ConsultationRequestedEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = 144360860780628572L;

  private final NominationId nominationId;

  public ConsultationRequestedEvent(NominationId nominationId) {
    super(nominationId);
    this.nominationId = nominationId;
  }

  public NominationId getNominationId() {
    return nominationId;
  }
}
