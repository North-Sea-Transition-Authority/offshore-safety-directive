package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdTestUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.NominationSubmittedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsService;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;

@ExtendWith(MockitoExtension.class)
class NominationSnsServiceTest {

  @Mock
  private SnsService snsService;

  private final SnsTopicArn nominationsTopicArn = new SnsTopicArn("test-nominations-topic-arn");

  private NominationSnsService nominationSnsService;

  @BeforeEach
  void setUp() {
    when(snsService.getOrCreateTopic(NominationSnsService.NOMINATIONS_TOPIC_NAME)).thenReturn(nominationsTopicArn);

    nominationSnsService = spy(new NominationSnsService(snsService));
  }

  @Test
  void handleNominationSubmitted() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var event = NominationSubmittedEventTestUtil.createEvent(nominationDetail);

    doNothing().when(nominationSnsService).publishNominationSubmittedMessage(nominationDetail);

    nominationSnsService.handleNominationSubmitted(event);

    verify(nominationSnsService).publishNominationSubmittedMessage(event.getNominationDetail());
  }

  @Test
  void publishNominationSubmittedMessage() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var correlationId = UUID.randomUUID().toString();

    CorrelationIdTestUtil.setCorrelationIdOnMdc(correlationId);

    nominationSnsService.publishNominationSubmittedMessage(nominationDetail);

    var epmqMessageArgumentCaptor = ArgumentCaptor.forClass(NominationSubmittedOsdEpmqMessage.class);

    verify(snsService).publishMessage(eq(nominationsTopicArn), epmqMessageArgumentCaptor.capture());

    var epmqMessage = epmqMessageArgumentCaptor.getValue();

    assertThat(epmqMessage.getNominationId()).isEqualTo(nominationDetail.getNomination().getId());
    assertThat(epmqMessage.getCorrelationId()).isEqualTo(correlationId);
  }
}
