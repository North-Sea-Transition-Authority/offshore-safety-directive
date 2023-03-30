package uk.co.nstauthority.offshoresafetydirective.wons;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsService;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsQueueUrl;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsService;

@Service
class WonsApplicationSqsService {

  static final String APPLICATIONS_SNS_TOPIC_NAME = "wons-applications";
  static final String APPLICATIONS_OSD_QUEUE_NAME = "wons-applications-osd";

  private final SnsService snsService;
  private final SnsTopicArn applicationsSnsTopicArn;
  private final SqsQueueUrl applicationsOsdQueueUrl;

  WonsApplicationSqsService(SqsService sqsService, SnsService snsService) {
    this.snsService = snsService;

    applicationsSnsTopicArn = snsService.getOrCreateTopic(APPLICATIONS_SNS_TOPIC_NAME);
    applicationsOsdQueueUrl = sqsService.getOrCreateQueue(APPLICATIONS_OSD_QUEUE_NAME);
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void subscribeSnsTopicToOsdQueue() {
    snsService.subscribeTopicToSqsQueue(applicationsSnsTopicArn, applicationsOsdQueueUrl);
  }
}
