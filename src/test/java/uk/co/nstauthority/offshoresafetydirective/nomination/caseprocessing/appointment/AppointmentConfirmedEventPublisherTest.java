package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.EventObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@ExtendWith(MockitoExtension.class)
class AppointmentConfirmedEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private AppointmentConfirmedEventPublisher appointmentConfirmedEventPublisher;

  @Test
  void publish() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var expectedResult = new NominationId(nominationDetail.getNomination().getId());
    appointmentConfirmedEventPublisher.publish(nominationDetail);

    var captor = ArgumentCaptor.forClass(AppointmentConfirmedEvent.class);
    verify(applicationEventPublisher).publishEvent(captor.capture());

    assertThat(captor.getValue())
        .extracting(EventObject::getSource)
        .isEqualTo(expectedResult);
  }
}