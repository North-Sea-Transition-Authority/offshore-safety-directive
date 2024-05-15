package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.EventObject;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;

@ExtendWith(MockitoExtension.class)
class AppointmentTerminationEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private AppointmentTerminationEventPublisher appointmentTerminationEventPublisher;

  @Test
  void publish() {
    var appointmentId = UUID.randomUUID();
    appointmentTerminationEventPublisher.publish(new AppointmentId(appointmentId));

    var captor = ArgumentCaptor.forClass(AppointmentTerminationEvent.class);
    verify(applicationEventPublisher).publishEvent(captor.capture());

    assertThat(captor.getValue())
        .extracting(EventObject::getSource)
        .isEqualTo(new AppointmentId(appointmentId));
  }
}