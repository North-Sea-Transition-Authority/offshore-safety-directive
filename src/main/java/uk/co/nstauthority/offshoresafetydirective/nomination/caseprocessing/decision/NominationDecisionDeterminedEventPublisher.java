package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@Component
class NominationDecisionDeterminedEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(NominationDecisionDeterminedEventPublisher.class);

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  NominationDecisionDeterminedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publish(NominationId nominationId) {
    applicationEventPublisher.publishEvent(
        new NominationDecisionDeterminedEvent(nominationId)
    );
    LOGGER.info("Published NominationDecisionDeterminedEvent for nomination with ID {}", nominationId.id());
  }

}
