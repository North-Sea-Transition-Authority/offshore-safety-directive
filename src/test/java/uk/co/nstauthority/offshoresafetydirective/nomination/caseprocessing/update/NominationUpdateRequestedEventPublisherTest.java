package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

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
class NominationUpdateRequestedEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private NominationUpdateRequestedEventPublisher nominationUpdateRequestedEventPublisher;

  @Test
  void publish_verifyCalls() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationId = new NominationId(nominationDetail.getNomination().getId());
    nominationUpdateRequestedEventPublisher.publish(new NominationId(nominationDetail));

    var captor = ArgumentCaptor.forClass(NominationUpdateRequestedEvent.class);
    verify(applicationEventPublisher).publishEvent(captor.capture());

    assertThat(captor.getValue())
        .extracting(NominationUpdateRequestedEvent::getNominationId)
        .isEqualTo(nominationId);
  }
}