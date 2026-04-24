package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class NominationWithdrawalEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(NominationWithdrawalEventPublisher.class);

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  NominationWithdrawalEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publish(NominationId nominationId) {
    applicationEventPublisher.publishEvent(new NominationWithdrawalEvent(nominationId));
    LOGGER.info("Published NominationWithdrawalEvent for nomination with ID {}", nominationId.id());
  }
}
