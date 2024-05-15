package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons;

import java.util.concurrent.TimeUnit;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessageTypeMapping;
import uk.co.fivium.energyportalmessagequeue.message.EpmqTopics;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.WonsGeologicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsQueueUrl;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;

@Service
class WonsNotificationSqsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WonsNotificationSqsService.class);
  static final String WONS_NOTIFICATIONS_QUEUE_NAME = "wons-notifications";

  private final SnsService snsService;
  private final SqsService sqsService;
  private final MetricsProvider metricsProvider;
  private final SnsTopicArn notificationsSnsTopicArn;
  private final SqsQueueUrl notificationsOsdQueueUrl;
  private final WonsNotificationCompletedService wonsNotificationCompletedService;

  WonsNotificationSqsService(SnsService snsService, SqsService sqsService, MetricsProvider metricsProvider,
                             WonsNotificationCompletedService wonsNotificationCompletedService) {
    this.snsService = snsService;
    this.sqsService = sqsService;
    this.metricsProvider = metricsProvider;
    this.wonsNotificationCompletedService = wonsNotificationCompletedService;
    notificationsSnsTopicArn = snsService.getOrCreateTopic(EpmqTopics.WONS_NOTIFICATIONS.getName());
    notificationsOsdQueueUrl = sqsService.getOrCreateQueue(WONS_NOTIFICATIONS_QUEUE_NAME);
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void subscribeSnsTopicToOsdQueue() {
    snsService.subscribeTopicToSqsQueue(notificationsSnsTopicArn, notificationsOsdQueueUrl);
  }

  @Scheduled(fixedDelayString = "${epmq.message-poll-interval-seconds}", timeUnit = TimeUnit.SECONDS)
  @SchedulerLock(name = "WonsNotificationSqsService")
  void receiveMessages() {
    LOGGER.debug("Process received WONS notification messages");
    sqsService.receiveQueueMessages(
        notificationsOsdQueueUrl,
        EpmqMessageTypeMapping.getTypeToClassMapByTopic(EpmqTopics.WONS_NOTIFICATIONS),
        epmqMessage -> {
          metricsProvider.getWonsNotificationMessagesReceivedCounter().increment();
          if (epmqMessage instanceof WonsGeologicalSidetrackNotificationCompletedEpmqMessage message) {
            LOGGER.info(
                "Started processing WonsGeologicalSidetrackNotificationCompletedEpmqMessage with notification id {} " +
                    "and correlation id {}",
                message.getNotificationId(),
                message.getCorrelationId()
            );
            wonsNotificationCompletedService.processParentWellboreNotification(message);
            LOGGER.info(
                "Finished processing WonsGeologicalSidetrackNotificationCompletedEpmqMessage with notification id {}" +
                    "and correlation id {}",
                message.getNotificationId(),
                message.getCorrelationId()
            );
          }
        });
  }

}
