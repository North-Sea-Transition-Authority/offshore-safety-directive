package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

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
class NominationDecisionDeterminedEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private NominationDecisionDeterminedEventPublisher nominationDecisionDeterminedEventPublisher;

  @Captor
  private ArgumentCaptor<NominationDecisionDeterminedEvent> nominationDecisionDeterminedEventArgumentCaptor;

  @Test
  void publish_verifyEventPublished() {

    var nominationId = new NominationId(123);

    nominationDecisionDeterminedEventPublisher.publish(nominationId);

    then(applicationEventPublisher)
        .should(onlyOnce())
        .publishEvent(nominationDecisionDeterminedEventArgumentCaptor.capture());

    assertThat(nominationDecisionDeterminedEventArgumentCaptor.getValue().getNominationId()).isEqualTo(nominationId);
  }

}