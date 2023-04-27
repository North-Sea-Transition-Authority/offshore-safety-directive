package uk.co.nstauthority.offshoresafetydirective.wons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalmessagequeue.message.EpmqTopics;
import uk.co.fivium.energyportalmessagequeue.message.wons.WonsApplicationSubmittedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsQueueUrl;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;

@Service
@Profile("!disable-epmq")
class WonsApplicationSqsService {

  static final String APPLICATIONS_OSD_QUEUE_NAME = "wons-applications-osd";

  private final SqsService sqsService;
  private final SnsService snsService;
  private final SnsTopicArn applicationsSnsTopicArn;
  private final SqsQueueUrl applicationsOsdQueueUrl;

  @Autowired
  WonsApplicationSqsService(SqsService sqsService, SnsService snsService) {
    this.sqsService = sqsService;
    this.snsService = snsService;

    applicationsSnsTopicArn = snsService.getOrCreateTopic(EpmqTopics.WONS_APPLICATIONS.getName());
    applicationsOsdQueueUrl = sqsService.getOrCreateQueue(APPLICATIONS_OSD_QUEUE_NAME);
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void subscribeSnsTopicToOsdQueue() {
    snsService.subscribeTopicToSqsQueue(applicationsSnsTopicArn, applicationsOsdQueueUrl);
  }

  @Scheduled(fixedDelay = 5000L)
  void receiveMessages() {
    sqsService.receiveQueueMessages(applicationsOsdQueueUrl, WonsApplicationSubmittedEpmqMessage.class, message -> {
      // TODO: Handle messages as part of https://ogajira.atlassian.net/browse/OSDOP-17
    });
  }
}
