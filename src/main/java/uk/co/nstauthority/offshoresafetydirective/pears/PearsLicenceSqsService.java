package uk.co.nstauthority.offshoresafetydirective.pears;

import java.util.concurrent.TimeUnit;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalmessagequeue.message.EpmqTopics;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsCorrectionAppliedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsQueueUrl;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;

@Service
@Profile("!disable-epmq")
class PearsLicenceSqsService {

  static final String LICENCES_OSD_QUEUE_NAME = "pears-licences-osd";
  private static final Logger LOGGER = LoggerFactory.getLogger(PearsLicenceSqsService.class);

  private final SqsService sqsService;
  private final SnsService snsService;
  private final SnsTopicArn licencesSnsTopicArn;
  private final SqsQueueUrl licencesOsdQueueUrl;
  private final PearsLicenceService pearsLicenceService;

  @Autowired
  PearsLicenceSqsService(SqsService sqsService, SnsService snsService, PearsLicenceService pearsLicenceService) {
    this.sqsService = sqsService;
    this.snsService = snsService;

    licencesSnsTopicArn = snsService.getOrCreateTopic(EpmqTopics.PEARS_LICENCES.getName());
    licencesOsdQueueUrl = sqsService.getOrCreateQueue(LICENCES_OSD_QUEUE_NAME);
    this.pearsLicenceService = pearsLicenceService;
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void subscribeSnsTopicToOsdQueue() {
    snsService.subscribeTopicToSqsQueue(licencesSnsTopicArn, licencesOsdQueueUrl);
  }

  @Scheduled(fixedDelayString = "${epmq.message-poll-interval-seconds}", timeUnit = TimeUnit.SECONDS)
  @SchedulerLock(name = "PearsLicenceSqsService_receiveMessages")
  void receiveMessages() {
    LOGGER.info("Process received PEARS licence messages");
    sqsService.receiveQueueMessages(
        licencesOsdQueueUrl,
        // TODO OSDOP-114 - Change this to support multiple messages on the same topic
        PearsCorrectionAppliedEpmqMessage.class,
        pearsLicenceService::handlePearsCorrectionApplied
    );
  }
}
