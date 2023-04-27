package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@Component
class ConsultationRequestEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsultationRequestEventPublisher.class);

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  ConsultationRequestEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publish(NominationId nominationId) {
    applicationEventPublisher.publishEvent(
        new ConsultationRequestedEvent(nominationId)
    );
    LOGGER.info("Published ConsultationRequestedEvent for nomination with ID {}", nominationId.id());
  }
}
