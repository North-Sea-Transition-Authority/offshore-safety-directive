package uk.co.nstauthority.offshoresafetydirective.wons;

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
import uk.co.fivium.energyportalmessagequeue.message.wons.WonsApplicationSubmittedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsQueueUrl;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;

@Service
@Profile("!disable-epmq")
class WonsApplicationSqsService {

  static final String APPLICATIONS_OSD_QUEUE_NAME = "wons-applications-osd";
  private static final Logger LOGGER = LoggerFactory.getLogger(WonsApplicationSqsService.class);

  private final SqsService sqsService;
  private final SnsService snsService;
  private final SnsTopicArn applicationsSnsTopicArn;
  private final SqsQueueUrl applicationsOsdQueueUrl;
  private final MetricsProvider metricsProvider;

  @Autowired
  WonsApplicationSqsService(SqsService sqsService, SnsService snsService, MetricsProvider metricsProvider) {
    this.sqsService = sqsService;
    this.snsService = snsService;

    applicationsSnsTopicArn = snsService.getOrCreateTopic(EpmqTopics.WONS_APPLICATIONS.getName());
    applicationsOsdQueueUrl = sqsService.getOrCreateQueue(APPLICATIONS_OSD_QUEUE_NAME);
    this.metricsProvider = metricsProvider;
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void subscribeSnsTopicToOsdQueue() {
    snsService.subscribeTopicToSqsQueue(applicationsSnsTopicArn, applicationsOsdQueueUrl);
  }

  @Scheduled(fixedDelayString = "${epmq.message-poll-interval-seconds}", timeUnit = TimeUnit.SECONDS)
  @SchedulerLock(name = "WonsApplicationSqsService_receiveMessages")
  void receiveMessages() {
    LOGGER.debug("Process received WONS application messages");
    sqsService.receiveQueueMessages(
        applicationsOsdQueueUrl,
        WonsApplicationSubmittedEpmqMessage.class,
        message ->
          // TODO: Handle messages as part of https://ogajira.atlassian.net/browse/OSDOP-17
          metricsProvider.getWonsApplicationMessagesReceivedCounter().increment()
    );
  }
}
