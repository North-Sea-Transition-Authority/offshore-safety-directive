package uk.co.nstauthority.offshoresafetydirective.pears;

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
import uk.co.fivium.energyportalmessagequeue.message.EpmqTopics;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsCorrectionAppliedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsQueueUrl;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;

@ExtendWith(MockitoExtension.class)
class PearsLicenceSqsServiceTest {

  @Mock
  private SqsService sqsService;

  @Mock
  private SnsService snsService;

  @Mock
  private PearsLicenceService pearsLicenceService;

  @Mock
  private MetricsProvider metricsProvider;

  @Captor
  private ArgumentCaptor<Consumer<PearsCorrectionAppliedEpmqMessage>> correctionAppliedEpmqMessageConsumerCaptor;

  private final SnsTopicArn licencesSnsTopicArn = new SnsTopicArn("test-licences-sns-topic-arn");
  private final SqsQueueUrl licencesOsdQueueUrl = new SqsQueueUrl("test-licences-osd-queue-url");

  private PearsLicenceSqsService pearsLicenceSqsService;
  private Counter counter;

  @BeforeEach
  void setUp() {
    when(snsService.getOrCreateTopic(EpmqTopics.PEARS_LICENCES.getName()))
        .thenReturn(licencesSnsTopicArn);
    when(sqsService.getOrCreateQueue(PearsLicenceSqsService.LICENCES_OSD_QUEUE_NAME))
        .thenReturn(licencesOsdQueueUrl);

    counter = mock(Counter.class);

    pearsLicenceSqsService = new PearsLicenceSqsService(sqsService, snsService, pearsLicenceService, metricsProvider);
  }

  @Test
  void subscribeSnsTopicToOsdQueue() {
    pearsLicenceSqsService.subscribeSnsTopicToOsdQueue();

    verify(snsService).subscribeTopicToSqsQueue(licencesSnsTopicArn, licencesOsdQueueUrl);
  }

  @Test
  void receiveMessages() {
    pearsLicenceSqsService.receiveMessages();

    verify(sqsService).receiveQueueMessages(
        eq(licencesOsdQueueUrl),
        eq(PearsCorrectionAppliedEpmqMessage.class),
        any()
    );
  }

  @Test
  void receiveMessages_verifyCalls() {

    var createdInstantOfMessage1 = Instant.now();
    var createdInstantOfMessage2 = Instant.now().plusSeconds(10);

    var message1 = new PearsCorrectionAppliedEpmqMessage(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        createdInstantOfMessage1
    );
    var message2 = new PearsCorrectionAppliedEpmqMessage(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        createdInstantOfMessage2
    );

    doAnswer(invocation -> {
      var onMessage = correctionAppliedEpmqMessageConsumerCaptor.getValue();
      onMessage.accept(message1);
      onMessage.accept(message2);
      return null;
    })
        .when(sqsService)
        .receiveQueueMessages(
            eq(licencesOsdQueueUrl),
            eq(PearsCorrectionAppliedEpmqMessage.class),
            correctionAppliedEpmqMessageConsumerCaptor.capture()
        );

    when(metricsProvider.getPearsLicenceMessagesReceivedCounter()).thenReturn(counter);


    pearsLicenceSqsService.receiveMessages();

    verify(pearsLicenceService, times(1)).handlePearsCorrectionApplied(message1);
    verify(pearsLicenceService, times(1)).handlePearsCorrectionApplied(message2);

    verify(metricsProvider, times(2)).getPearsLicenceMessagesReceivedCounter();
    verify(counter, times(2)).increment();
    verifyNoMoreInteractions(metricsProvider);
  }
}
