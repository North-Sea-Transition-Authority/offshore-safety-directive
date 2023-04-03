package uk.co.nstauthority.offshoresafetydirective.pears;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsService;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsQueueUrl;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsService;

@Service
class PearsLicenceSqsService {

  static final String LICENCES_SNS_TOPIC_NAME = "pears-licences";
  static final String LICENCES_OSD_QUEUE_NAME = "pears-licences-osd";

  private final SnsService snsService;
  private final SnsTopicArn licencesSnsTopicArn;
  private final SqsQueueUrl licencesOsdQueueUrl;

  @Autowired
  PearsLicenceSqsService(SqsService sqsService, SnsService snsService) {
    this.snsService = snsService;

    licencesSnsTopicArn = snsService.getOrCreateTopic(LICENCES_SNS_TOPIC_NAME);
    licencesOsdQueueUrl = sqsService.getOrCreateQueue(LICENCES_OSD_QUEUE_NAME);
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void subscribeSnsTopicToOsdQueue() {
    snsService.subscribeTopicToSqsQueue(licencesSnsTopicArn, licencesOsdQueueUrl);
  }
}
