package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;

@Component
class ConsulteeNotificationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulteeNotificationEventListener.class);

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleConsultationRequestEvent(ConsultationRequestedEvent consultationRequestedEvent) {
    LOGGER.info(
        "Handling ConsultationRequestedEvent for nomination with ID {}",
        consultationRequestedEvent.getNominationId().id()
    );
  }
}
