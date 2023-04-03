package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsLicenceProcessedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsService;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsQueueUrl;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsService;

@ExtendWith(MockitoExtension.class)
class PearsLicenceSqsServiceTest {

  @Mock
  private SqsService sqsService;

  @Mock
  private SnsService snsService;

  private final SnsTopicArn licencesSnsTopicArn = new SnsTopicArn("test-licences-sns-topic-arn");
  private final SqsQueueUrl licencesOsdQueueUrl = new SqsQueueUrl("test-licences-osd-queue-url");

  private PearsLicenceSqsService pearsLicenceSqsService;

  @BeforeEach
  void setUp() {
    when(snsService.getOrCreateTopic(PearsLicenceSqsService.LICENCES_SNS_TOPIC_NAME))
        .thenReturn(licencesSnsTopicArn);
    when(sqsService.getOrCreateQueue(PearsLicenceSqsService.LICENCES_OSD_QUEUE_NAME))
        .thenReturn(licencesOsdQueueUrl);

    pearsLicenceSqsService = new PearsLicenceSqsService(sqsService, snsService);
  }

  @Test
  void subscribeSnsTopicToOsdQueue() {
    pearsLicenceSqsService.subscribeSnsTopicToOsdQueue();

    verify(snsService).subscribeTopicToSqsQueue(licencesSnsTopicArn, licencesOsdQueueUrl);
  }

  @Test
  void receiveMessages() {
    pearsLicenceSqsService.receiveMessages();

    verify(sqsService).receiveQueueMessages(eq(licencesOsdQueueUrl), eq(PearsLicenceProcessedEpmqMessage.class), any());
  }
}
