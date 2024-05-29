package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessageTypeMapping;
import uk.co.fivium.energyportalmessagequeue.message.EpmqTopics;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.WonsNotificationCompletedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.WonsNotificationType;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.geological.WonsGeologicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.mechanical.WonsMechicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.respud.WonsRespudNotificationCompletedEpmqMessage;
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
  private ArgumentCaptor<Consumer<EpmqMessage>> completedMessageCaptor;

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
  void receiveMessages_whenGeologicalSidetrackNotificationMessage() {

    givenMessageReceivedCounterExists();

    var message1 = givenGeologicalSidetrackNotificationMessage();
    var message2 = givenGeologicalSidetrackNotificationMessage();

    givenMessagesExistOnTopic(Set.of(message1, message2));

    wonsNotificationSqsService.receiveMessages();

    verify(wonsNotificationCompletedService, times(1)).processParentWellboreNotification(message1);
    verify(wonsNotificationCompletedService, times(1)).processParentWellboreNotification(message2);

    thenWeIncrementMessageReceivedCounterBy(2);
  }

  @Test
  void receiveMessages_whenMechanicalSidetrackNotificationMessage() {

    givenMessageReceivedCounterExists();

    var message1 = givenMechanicalSidetrackNotificationMessage();
    var message2 = givenMechanicalSidetrackNotificationMessage();

    givenMessagesExistOnTopic(Set.of(message1, message2));

    wonsNotificationSqsService.receiveMessages();

    verify(wonsNotificationCompletedService, times(1)).processParentWellboreNotification(message1);
    verify(wonsNotificationCompletedService, times(1)).processParentWellboreNotification(message2);

    thenWeIncrementMessageReceivedCounterBy(2);
  }

  @Test
  void receiveMessages_whenRespudNotificationMessage() {

    givenMessageReceivedCounterExists();

    var message1 = givenRespudNotificationMessage();
    var message2 = givenRespudNotificationMessage();
    var message3 = givenRespudNotificationMessage();

    givenMessagesExistOnTopic(Set.of(message1, message2, message3));

    wonsNotificationSqsService.receiveMessages();

    verify(wonsNotificationCompletedService, times(1)).processParentWellboreNotification(message1);
    verify(wonsNotificationCompletedService, times(1)).processParentWellboreNotification(message2);
    verify(wonsNotificationCompletedService, times(1)).processParentWellboreNotification(message3);

    thenWeIncrementMessageReceivedCounterBy(3);
  }

  @Test
  void receiveMessages_whenNonChildProducingNotificationMessage() {

    givenMessageReceivedCounterExists();

    var message = givenNonChildProducingNotificationMessage();

    givenMessagesExistOnTopic(Set.of(message));

    wonsNotificationSqsService.receiveMessages();

    verifyNoInteractions(wonsNotificationCompletedService);

    thenWeIncrementMessageReceivedCounterBy(1);
  }

  private void givenMessagesExistOnTopic(Set<EpmqMessage> epmqMessages) {
    doAnswer(invocation -> {
      var onMessage = completedMessageCaptor.getValue();
      epmqMessages.forEach(onMessage);
      return null;
    })
        .when(sqsService)
        .receiveQueueMessages(
            eq(QUEUE_URL),
            eq(EpmqMessageTypeMapping.getTypeToClassMapByTopic(EpmqTopics.WONS_NOTIFICATIONS)),
            completedMessageCaptor.capture()
        );
  }

  private void givenMessageReceivedCounterExists() {
    when(metricsProvider.getWonsNotificationMessagesReceivedCounter())
        .thenReturn(counter);
  }

  private void thenWeIncrementMessageReceivedCounterBy(int counterIncrement) {
    verify(metricsProvider, times(counterIncrement)).getWonsNotificationMessagesReceivedCounter();
    verify(counter, times(counterIncrement)).increment();
  }

  private WonsGeologicalSidetrackNotificationCompletedEpmqMessage givenGeologicalSidetrackNotificationMessage() {
    return new WonsGeologicalSidetrackNotificationCompletedEpmqMessage(
        "correlation-%s".formatted(UUID.randomUUID()),
        Instant.now(),
        "notification-%s".formatted(UUID.randomUUID()),
        100, // submitted on wellbore ID
        200, // ID of wellbore created by notification
        true // using parent wellbore appointment
    );
  }

  private WonsMechicalSidetrackNotificationCompletedEpmqMessage givenMechanicalSidetrackNotificationMessage() {
    return new WonsMechicalSidetrackNotificationCompletedEpmqMessage(
        "correlation-%s".formatted(UUID.randomUUID()),
        Instant.now(),
        "notification-%s".formatted(UUID.randomUUID()),
        100, // submitted on wellbore ID
        200, // ID of wellbore created by notification
        true // using parent wellbore appointment
    );
  }

  private WonsRespudNotificationCompletedEpmqMessage givenRespudNotificationMessage() {
    return new WonsRespudNotificationCompletedEpmqMessage(
        "correlation-%s".formatted(UUID.randomUUID()),
        Instant.now(),
        "notification-%s".formatted(UUID.randomUUID()),
        100, // submitted on wellbore ID
        200, // ID of wellbore created by notification
        true // using parent wellbore appointment
    );
  }

  private WonsNotificationCompletedEpmqMessage givenNonChildProducingNotificationMessage() {
    return new WonsNotificationCompletedEpmqMessage(
        WonsNotificationCompletedEpmqMessage.TYPE,
        "correlation-%s".formatted(UUID.randomUUID()),
        Instant.now(),
        "notification-%s".formatted(UUID.randomUUID()),
        WonsNotificationType.WELL_TEST,
        200
    );
  }
}