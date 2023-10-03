package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class AppointmentRemovedEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentRemovedEventPublisher.class);
  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  AppointmentRemovedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publish(AppointmentId appointmentId) {
    applicationEventPublisher.publishEvent(
        new AppointmentRemovedEvent(appointmentId)
    );
    LOGGER.info("Published AppointmentRemovedEvent for appointment with ID {}", appointmentId.id());
  }
}
