package uk.co.nstauthority.offshoresafetydirective.nomination;

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

@ExtendWith(MockitoExtension.class)
class NominationWithdrawalEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private NominationWithdrawalEventPublisher nominationWithdrawalEventPublisher;

  @Captor
  private ArgumentCaptor<NominationWithdrawalEvent> withdrawNominationEventArgumentCaptor;

  @Test
  void publish_verifyInteractions() {

    var nominationId = new NominationId(UUID.randomUUID());

    nominationWithdrawalEventPublisher.publish(nominationId);

    then(applicationEventPublisher)
        .should(onlyOnce())
        .publishEvent(withdrawNominationEventArgumentCaptor.capture());

    assertThat(withdrawNominationEventArgumentCaptor.getValue().getNominationId()).isEqualTo(nominationId);

  }

}