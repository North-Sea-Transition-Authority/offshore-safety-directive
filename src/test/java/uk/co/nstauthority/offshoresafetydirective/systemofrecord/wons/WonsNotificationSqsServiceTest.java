package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessageTypeMapping;
import uk.co.fivium.energyportalmessagequeue.message.EpmqTopics;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.WonsGeologicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsQueueUrl;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;

@ExtendWith(MockitoExtension.class)
class WonsNotificationSqsServiceTest {

  private static final SnsTopicArn TOPIC_ARN = new SnsTopicArn("topic");
  private static final SqsQueueUrl QUEUE_URL = new SqsQueueUrl("queue");

  @Mock
  private SnsService snsService;

  @Mock
  private SqsService sqsService;

  @Mock
  private MetricsProvider metricsProvider;

  @Mock
  private WonsNotificationCompletedService wonsNotificationCompletedService;

  @Captor
  private ArgumentCaptor<Consumer<WonsGeologicalSidetrackNotificationCompletedEpmqMessage>> completedMessageCaptor;

  private WonsNotificationSqsService wonsNotificationSqsService;
  private Counter counter;

  @BeforeEach
  void setUp() {
    when(snsService.getOrCreateTopic(EpmqTopics.WONS_NOTIFICATIONS.getName()))
        .thenReturn(TOPIC_ARN);
    when(sqsService.getOrCreateQueue(WonsNotificationSqsService.WONS_NOTIFICATIONS_QUEUE_NAME))
        .thenReturn(QUEUE_URL);

    counter = mock(Counter.class);

    wonsNotificationSqsService = new WonsNotificationSqsService(
        snsService,
        sqsService,
        metricsProvider,
        wonsNotificationCompletedService
    );
  }

  @Test
  void subscribeSnsTopicToOsdQueue() {
    wonsNotificationSqsService.subscribeSnsTopicToOsdQueue();
    verify(snsService).subscribeTopicToSqsQueue(TOPIC_ARN, QUEUE_URL);
  }

  @Test
  void receiveMessages() {
    wonsNotificationSqsService.receiveMessages();

    verify(sqsService).receiveQueueMessages(
        eq(QUEUE_URL),
        eq(EpmqMessageTypeMapping.getTypeToClassMapByTopic(EpmqTopics.WONS_NOTIFICATIONS)),
        any()
    );
  }

  @Test
  void receiveMessages_whenNotificationCompletedMessage_verifyCalls() {

    when(metricsProvider.getWonsNotificationMessagesReceivedCounter())
        .thenReturn(counter);

    var createdInstantOfMessage1 = Instant.now();
    var createdInstantOfMessage2 = Instant.now().plusSeconds(10);

    var message1 = new WonsGeologicalSidetrackNotificationCompletedEpmqMessage(
        "correlation-%s".formatted(UUID.randomUUID()),
        createdInstantOfMessage1,
        "notification-%s".formatted(UUID.randomUUID()),
        100,
        200,
        true
    );
    var message2 = new WonsGeologicalSidetrackNotificationCompletedEpmqMessage(
        "correlation-%s".formatted(UUID.randomUUID()),
        createdInstantOfMessage2,
        "notification-%s".formatted(UUID.randomUUID()),
        300,
        400,
        false
    );

    doAnswer(invocation -> {
      var onMessage = completedMessageCaptor.getValue();
      onMessage.accept(message1);
      onMessage.accept(message2);
      return null;
    })
        .when(sqsService)
        .receiveQueueMessages(
            eq(QUEUE_URL),
            eq(EpmqMessageTypeMapping.getTypeToClassMapByTopic(EpmqTopics.WONS_NOTIFICATIONS)),
            completedMessageCaptor.capture()
        );

    when(metricsProvider.getWonsNotificationMessagesReceivedCounter()).thenReturn(counter);

    wonsNotificationSqsService.receiveMessages();

    verify(wonsNotificationCompletedService, times(1)).processParentWellboreNotification(message1);
    verify(wonsNotificationCompletedService, times(1)).processParentWellboreNotification(message2);

    verify(metricsProvider, times(2)).getWonsNotificationMessagesReceivedCounter();
    verify(counter, times(2)).increment();

    verifyNoMoreInteractions(wonsNotificationCompletedService, metricsProvider);
  }
}