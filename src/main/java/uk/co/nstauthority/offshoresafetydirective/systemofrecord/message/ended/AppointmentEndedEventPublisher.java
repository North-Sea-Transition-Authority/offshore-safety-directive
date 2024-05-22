package uk.co.nstauthority.offshoresafetydirective.systemofrecord.message.ended;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEndedEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentEndedEventPublisher.class);

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public AppointmentEndedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void publish(UUID appointmentId) {
    applicationEventPublisher.publishEvent(new AppointmentEndedEvent(appointmentId));
    LOGGER.info("Published AppointmentEndedEvent for appointment with ID {}", appointmentId);
  }
}
