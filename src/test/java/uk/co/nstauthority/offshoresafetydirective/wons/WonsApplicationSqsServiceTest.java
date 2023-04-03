package uk.co.nstauthority.offshoresafetydirective.wons;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalmessagequeue.message.wons.WonsApplicationSubmittedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsService;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsQueueUrl;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsService;

@ExtendWith(MockitoExtension.class)
class WonsApplicationSqsServiceTest {

  @Mock
  private SqsService sqsService;

  @Mock
  private SnsService snsService;

  private final SnsTopicArn applicationsSnsTopicArn = new SnsTopicArn("test-applications-sns-topic-arn");
  private final SqsQueueUrl applicationsOsdQueueUrl = new SqsQueueUrl("test-applications-osd-queue-url");

  private WonsApplicationSqsService wonsApplicationSqsService;

  @BeforeEach
  void setUp() {
    when(snsService.getOrCreateTopic(WonsApplicationSqsService.APPLICATIONS_SNS_TOPIC_NAME))
        .thenReturn(applicationsSnsTopicArn);
    when(sqsService.getOrCreateQueue(WonsApplicationSqsService.APPLICATIONS_OSD_QUEUE_NAME))
        .thenReturn(applicationsOsdQueueUrl);

    wonsApplicationSqsService = new WonsApplicationSqsService(sqsService, snsService);
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
}
