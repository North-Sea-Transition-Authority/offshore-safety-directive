package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class NominationSubmittedEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  NominationSubmittedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publishNominationSubmittedEvent(NominationDetail nominationDetail) {
    var event = new NominationSubmittedEvent(this, nominationDetail);
    applicationEventPublisher.publishEvent(event);
  }
}
