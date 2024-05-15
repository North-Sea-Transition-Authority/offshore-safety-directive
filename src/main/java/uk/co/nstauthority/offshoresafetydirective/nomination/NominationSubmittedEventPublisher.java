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

  void publishNominationSubmittedEvent(NominationId nominationId) {
    var event = new NominationSubmittedEvent(nominationId);
    applicationEventPublisher.publishEvent(event);
  }
}
