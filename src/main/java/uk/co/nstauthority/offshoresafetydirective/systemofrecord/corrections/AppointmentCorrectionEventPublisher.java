package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;

@Component
class AppointmentCorrectionEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentCorrectionEventPublisher.class);
  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  AppointmentCorrectionEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publish(AppointmentId appointmentId) {
    applicationEventPublisher.publishEvent(new AppointmentCorrectionEvent(appointmentId));
    LOGGER.info("Published AppointmentCorrectionEvent for appointment with ID {}", appointmentId.id());
  }
}
