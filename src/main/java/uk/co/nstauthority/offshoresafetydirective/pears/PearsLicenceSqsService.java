package uk.co.nstauthority.offshoresafetydirective.pears;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalmessagequeue.message.EpmqTopics;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsLicenceProcessedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsQueueUrl;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;

@Service
@Profile("!disable-epmq")
class PearsLicenceSqsService {

  static final String LICENCES_OSD_QUEUE_NAME = "pears-licences-osd";

  private final SqsService sqsService;
  private final SnsService snsService;
  private final SnsTopicArn licencesSnsTopicArn;
  private final SqsQueueUrl licencesOsdQueueUrl;

  @Autowired
  PearsLicenceSqsService(SqsService sqsService, SnsService snsService) {
    this.sqsService = sqsService;
    this.snsService = snsService;

    licencesSnsTopicArn = snsService.getOrCreateTopic(EpmqTopics.PEARS_LICENCES.getName());
    licencesOsdQueueUrl = sqsService.getOrCreateQueue(LICENCES_OSD_QUEUE_NAME);
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void subscribeSnsTopicToOsdQueue() {
    snsService.subscribeTopicToSqsQueue(licencesSnsTopicArn, licencesOsdQueueUrl);
  }

  @Scheduled(fixedDelay = 5000L)
  void receiveMessages() {
    sqsService.receiveQueueMessages(licencesOsdQueueUrl, PearsLicenceProcessedEpmqMessage.class, message -> {
      // TODO: Handle messages as part of https://ogajira.atlassian.net/browse/OSDOP-20
    });
  }
}
