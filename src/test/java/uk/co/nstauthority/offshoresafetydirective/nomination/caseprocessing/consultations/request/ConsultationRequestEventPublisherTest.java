package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@ExtendWith(MockitoExtension.class)
class ConsultationRequestEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private ConsultationRequestEventPublisher consultationRequestEventPublisher;

  @Captor
  private ArgumentCaptor<ConsultationRequestedEvent> consultationRequestedEventArgumentCaptor;

  @Test
  void publish_verifyInteractions() {

    var nominationId = new NominationId(UUID.randomUUID());

    consultationRequestEventPublisher.publish(nominationId);

    then(applicationEventPublisher)
        .should(onlyOnce())
        .publishEvent(consultationRequestedEventArgumentCaptor.capture());

    assertThat(consultationRequestedEventArgumentCaptor.getValue().getNominationId()).isEqualTo(nominationId);

  }

}