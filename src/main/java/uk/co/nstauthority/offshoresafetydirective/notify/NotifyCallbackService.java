package uk.co.nstauthority.offshoresafetydirective.notify;

import java.util.Set;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.notify.library.FiviumNotificationClientApi;
import uk.co.fivium.notify.library.model.NotifyCallback;
import uk.co.fivium.notify.library.service.FiviumNotifyCallbackService;
import uk.co.nstauthority.offshoresafetydirective.configuration.EmailConfiguration;
import uk.gov.service.notify.NotificationClientException;

@Service
public class NotifyCallbackService {
  private static final Logger LOGGER = LoggerFactory.getLogger(NotifyCallbackService.class);

  private static final Set<NotifyCallback.NotifyCallbackStatus> FAILURE_STATUSES = Set.of(
      NotifyCallback.NotifyCallbackStatus.PERMANENT_FAILURE,
      NotifyCallback.NotifyCallbackStatus.TEMPORARY_FAILURE
  );

  private final NotifyEmailService notifyEmailService;
  private final FiviumNotificationClientApi fiviumNotificationClientApi;
  private final EmailConfiguration emailConfig;
  private final NotifyEmailBuilderService notifyEmailBuilderService;

  @Autowired
  public NotifyCallbackService(FiviumNotifyCallbackService fiviumNotifyCallbackService,
                               NotifyEmailService notifyEmailService,
                               FiviumNotificationClientApi fiviumNotificationClientApi,
                               EmailConfiguration emailConfig,
                               NotifyEmailBuilderService notifyEmailBuilderService) {
    this.notifyEmailService = notifyEmailService;
    this.fiviumNotificationClientApi = fiviumNotificationClientApi;
    this.emailConfig = emailConfig;
    this.notifyEmailBuilderService = notifyEmailBuilderService;

    fiviumNotifyCallbackService.registerCallbackObserver(this::handleNotifyCallback);
  }

  @Transactional
  public void handleNotifyCallback(NotifyCallback notifyCallback) {
    if (!FAILURE_STATUSES.contains(notifyCallback.getStatus())) {
      return;
    }

    LOGGER.info("The Notify provider could not deliver the message for notification ID {}.", notifyCallback.getId());

    var callbackEmail = emailConfig.callbackEmail();
    // if the failed email was going to the callback email address then return early
    if (notifyCallback.getTo().equals(callbackEmail)) {
      return;
    }

    try {
      var failedEmail = fiviumNotificationClientApi.getNotificationById(notifyCallback.getId());

      var failedNotifyEmail = notifyEmailBuilderService.builder(NotifyTemplate.EMAIL_DELIVERY_FAILED)
          .addPersonalisation(
              "FAILED_EMAIL_ADDRESS",
              failedEmail.getEmailAddress()
                  .orElseThrow(() -> new NotificationClientException(
                      "Couldn't extract recipient email address for failed notify notification with ID %s"
                          .formatted(failedEmail.getId().toString())
                  )
          ))
          .addPersonalisation("EMAIL_SUBJECT", failedEmail.getSubject().orElse(""))
          .addPersonalisation("EMAIL_BODY", failedEmail.getBody())
          .build();

      notifyEmailService.sendEmail(failedNotifyEmail, callbackEmail);

    } catch (NotificationClientException e) {
      LOGGER.error("Failing email properties cannot be retrieved", e);
    }
  }
}
