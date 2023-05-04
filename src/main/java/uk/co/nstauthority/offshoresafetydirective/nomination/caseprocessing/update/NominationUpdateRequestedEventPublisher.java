package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@Component
class NominationUpdateRequestedEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  NominationUpdateRequestedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publish(NominationId nominationId) {
    applicationEventPublisher.publishEvent(
        new NominationUpdateRequestedEvent(nominationId)
    );
  }

}
