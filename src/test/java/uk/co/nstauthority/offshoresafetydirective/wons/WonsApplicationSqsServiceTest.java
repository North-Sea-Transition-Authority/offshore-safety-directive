package uk.co.nstauthority.offshoresafetydirective.wons;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalmessagequeue.message.EpmqTopics;
import uk.co.fivium.energyportalmessagequeue.message.wons.WonsApplicationSubmittedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsQueueUrl;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;

@ExtendWith(MockitoExtension.class)
class WonsApplicationSqsServiceTest {

  @Mock
  private SqsService sqsService;

  @Mock
  private SnsService snsService;

  @Mock
  private MetricsProvider metricsProvider;

  @Captor
  private ArgumentCaptor<Consumer<WonsApplicationSubmittedEpmqMessage>> wonsApplicationEpmqMessageConsumerCaptor;

  private final SnsTopicArn applicationsSnsTopicArn = new SnsTopicArn("test-applications-sns-topic-arn");
  private final SqsQueueUrl applicationsOsdQueueUrl = new SqsQueueUrl("test-applications-osd-queue-url");

  private WonsApplicationSqsService wonsApplicationSqsService;
  private Counter counter;

  @BeforeEach
  void setUp() {
    when(snsService.getOrCreateTopic(EpmqTopics.WONS_APPLICATIONS.getName()))
        .thenReturn(applicationsSnsTopicArn);
    when(sqsService.getOrCreateQueue(WonsApplicationSqsService.APPLICATIONS_OSD_QUEUE_NAME))
        .thenReturn(applicationsOsdQueueUrl);

    counter = mock(Counter.class);
    wonsApplicationSqsService = new WonsApplicationSqsService(sqsService, snsService, metricsProvider);
  }

  @Test
  void subscribeSnsTopicToOsdQueue() {
    wonsApplicationSqsService.subscribeSnsTopicToOsdQueue();

    verify(snsService).subscribeTopicToSqsQueue(applicationsSnsTopicArn, applicationsOsdQueueUrl);
  }

  @Test
  void receiveMessages() {
    wonsApplicationSqsService.receiveMessages();

    verify(sqsService)
        .receiveQueueMessages(eq(applicationsOsdQueueUrl), eq(WonsApplicationSubmittedEpmqMessage.class), any());
  }

  @Test
  void receiveMessages_verifyCalls() {

    var createdInstantOfMessage1 = Instant.now();

    var message1 = new WonsApplicationSubmittedEpmqMessage(
        1,
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        createdInstantOfMessage1
    );

    doAnswer(invocation -> {
      var onMessage = wonsApplicationEpmqMessageConsumerCaptor.getValue();
      onMessage.accept(message1);
      return null;
    })
        .when(sqsService)
        .receiveQueueMessages(
            eq(applicationsOsdQueueUrl),
            eq(WonsApplicationSubmittedEpmqMessage.class),
            wonsApplicationEpmqMessageConsumerCaptor.capture()
        );
    when(metricsProvider.getWonsApplicationMessagesReceivedCounter()).thenReturn(counter);

    wonsApplicationSqsService.receiveMessages();

    verify(metricsProvider).getWonsApplicationMessagesReceivedCounter();
    verify(counter).increment();
    verifyNoMoreInteractions(metricsProvider);
  }
}
