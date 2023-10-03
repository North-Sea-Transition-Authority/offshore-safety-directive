package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AppointmentAddedEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentAddedEventPublisher.class);

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public AppointmentAddedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void publish(AppointmentId appointmentId) {
    applicationEventPublisher.publishEvent(
        new ManualAppointmentAddedEvent(appointmentId)
    );
    LOGGER.info("Published AppointmentAddedEvent for appointment with ID {}", appointmentId.id());
  }

}

