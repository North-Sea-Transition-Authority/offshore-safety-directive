package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class NominationSubmittedEventPublisherTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private NominationSubmittedEventPublisher nominationSubmittedEventPublisher;

  @Test
  void publishNominationSubmittedEvent_verifyEventPublished() {
    nominationSubmittedEventPublisher.publishNominationSubmittedEvent(NOMINATION_DETAIL);

    var nominationSubmittedEventCaptor = ArgumentCaptor.forClass(NominationSubmittedEvent.class);
    verify(applicationEventPublisher, times(1)).publishEvent(nominationSubmittedEventCaptor.capture());

    var nominationSubmittedEvent = nominationSubmittedEventCaptor.getValue();
    assertEquals(NOMINATION_DETAIL, nominationSubmittedEvent.getNominationDetail());
  }
}