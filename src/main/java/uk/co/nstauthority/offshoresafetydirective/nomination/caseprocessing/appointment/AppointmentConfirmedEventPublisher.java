package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@Component
class AppointmentConfirmedEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentConfirmedEventPublisher.class);

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  AppointmentConfirmedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publish(NominationId nominationId) {
    applicationEventPublisher.publishEvent(
        new AppointmentConfirmedEvent(nominationId)
    );
    LOGGER.info("Published AppointmentConfirmedEvent for nomination with ID {}", nominationId.id());
  }

}
