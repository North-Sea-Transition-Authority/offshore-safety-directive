package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;

@Component
class AppointmentTerminationEventPublisher {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentTerminationEventPublisher.class);

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  AppointmentTerminationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publish(AppointmentId appointmentId) {
    applicationEventPublisher.publishEvent(
        new AppointmentTerminationEvent(appointmentId)
    );
    LOGGER.info("Published AppointmentTerminationEvent for appointment with ID {}", appointmentId.id());
  }
}
