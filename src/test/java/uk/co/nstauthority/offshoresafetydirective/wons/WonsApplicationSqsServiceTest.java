package uk.co.nstauthority.offshoresafetydirective.wons;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import uk.co.fivium.energyportalmessagequeue.message.wons.application.WonsApplicationSubmittedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.wons.application.WonsApplicationType;
import uk.co.fivium.energyportalmessagequeue.message.wons.application.WonsOsdOperatorApplicationSubmittedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsQueueUrl;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons.application.WonsApplicationSubmittedService;

@ExtendWith(MockitoExtension.class)
class WonsApplicationSqsServiceTest {

  @Mock
  private SqsService sqsService;

  @Mock
  private SnsService snsService;

  @Mock
  private MetricsProvider metricsProvider;

  @Mock
  private WonsApplicationSubmittedService wonsApplicationSubmittedService;

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

    wonsApplicationSqsService = new WonsApplicationSqsService(
        sqsService, snsService, metricsProvider,
        wonsApplicationSubmittedService
    );
  }

  @Test
  void subscribeSnsTopicToOsdQueue() {
    wonsApplicationSqsService.subscribeSnsTopicToOsdQueue();
    verify(snsService).subscribeTopicToSqsQueue(applicationsSnsTopicArn, applicationsOsdQueueUrl);
  }

  @Test
  void receiveMessages_whenOsdOperatorRelatedApplication() {

    givenMessageReceivedCounter();

    var wonsOsdRelatedApplicationSubmittedMessage = new WonsOsdOperatorApplicationSubmittedEpmqMessage(
        WonsOsdOperatorApplicationSubmittedEpmqMessage.TYPE,
        "correlation-id",
        Instant.now(),
        100, // wons application id
        WonsApplicationType.PROVISIONAL_DRILLING_APPLICATION,
        200, // submitted on wellbore id
        "osd-appointment-id"
    );

    whenMessageIsReceived(wonsOsdRelatedApplicationSubmittedMessage);

    wonsApplicationSqsService.receiveMessages();

    thenMessageReceivedCounterIsIncrementedBy(1);

    verify(wonsApplicationSubmittedService).processApplicationSubmittedEvent(
        wonsOsdRelatedApplicationSubmittedMessage.getApplicationId(),
        wonsOsdRelatedApplicationSubmittedMessage.getSubmittedOnWellboreId(),
        wonsOsdRelatedApplicationSubmittedMessage.getForwardAreaApprovalAppointmentId()
    );
  }

  @Test
  void receiveMessages_whenNotOsdOperatorRelated() {

    var createdInstantOfMessage = Instant.now();
    var applicationId = 123;
    var wellboreId = 456;

    var message = new WonsApplicationSubmittedEpmqMessage(
        WonsApplicationSubmittedEpmqMessage.TYPE,
        UUID.randomUUID().toString(),
        createdInstantOfMessage,
        applicationId,
        WonsApplicationType.SUSPENSION_APPLICATION,
        wellboreId
    );

    givenMessageReceivedCounter();

    whenMessageIsReceived(message);

    wonsApplicationSqsService.receiveMessages();

    thenMessageReceivedCounterIsIncrementedBy(1);

    verify(wonsApplicationSubmittedService, never()).processApplicationSubmittedEvent(
        anyInt(),
        anyInt(),
        anyString()
    );
  }

  private void whenMessageIsReceived(WonsApplicationSubmittedEpmqMessage message) {

    doAnswer(invocation -> {
      var onMessage = wonsApplicationEpmqMessageConsumerCaptor.getValue();
      onMessage.accept(message);
      return null;
    })
        .when(sqsService)
        .receiveQueueMessages(
            eq(applicationsOsdQueueUrl),
            eq(EpmqMessageTypeMapping.getTypeToClassMapByTopic(EpmqTopics.WONS_APPLICATIONS)),
            wonsApplicationEpmqMessageConsumerCaptor.capture()
        );
  }

  private void thenMessageReceivedCounterIsIncrementedBy(int expectedIncrement) {
    verify(counter, times(expectedIncrement)).increment();
  }

  private void givenMessageReceivedCounter() {
    counter = mock(Counter.class);
    when(metricsProvider.getWonsApplicationMessagesReceivedCounter()).thenReturn(counter);
  }
}
