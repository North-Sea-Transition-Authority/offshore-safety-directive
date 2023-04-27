package uk.co.nstauthority.offshoresafetydirective.notify;

import java.util.Arrays;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.co.fivium.notify.library.FiviumNotificationClientApi;
import uk.co.nstauthority.offshoresafetydirective.configuration.EmailConfiguration;
import uk.gov.service.notify.NotificationClientException;

@Service
@ConditionalOnProperty(name = "email.mode", havingValue = "test")
class TestNotifyEmailService implements NotifyEmailService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestNotifyEmailService.class);

  private final FiviumNotificationClientApi fiviumNotificationClientApi;
  private final NotifyEmailValidator notifyEmailValidator;
  private final EmailConfiguration emailConfig;

  public TestNotifyEmailService(FiviumNotificationClientApi fiviumNotificationClientApi,
                                NotifyEmailValidator notifyEmailValidator,
                                EmailConfiguration emailConfig) {
    this.fiviumNotificationClientApi = fiviumNotificationClientApi;
    this.notifyEmailValidator = notifyEmailValidator;
    this.emailConfig = emailConfig;
  }

  @Transactional
  @Override
  public void sendEmail(NotifyEmail notifyEmail, String toEmailAddress) {
    sendEmail(notifyEmail, toEmailAddress, null, null);
  }

  @Transactional
  @Override
  public void sendEmail(NotifyEmail notifyEmail,
                        String toEmailAddress,
                        String reference,
                        String emailReplyToId) {

    // Set the TEST_EMAIL personalisation when in the development service
    notifyEmail.sendAsTestEmail(true);
    var personalisation = notifyEmail.getPersonalisations();

    var recipients = Arrays.stream(emailConfig.testRecipientList().split(","))
        .map(String::trim)
        .toList();

    // If we have test recipients send the email to each
    for (var testRecipient : recipients) {
      if (!notifyEmailValidator.isValid(testRecipient)) {
        LOGGER.error("Recipient email is not a valid email address: {}", testRecipient);
        continue;
      }

      try {
        fiviumNotificationClientApi.sendEmail(
            notifyEmail.getTemplate().getTemplateName(), testRecipient, personalisation, reference, emailReplyToId);

      } catch (NotificationClientException e) {
        LOGGER.error("Error sending test email", e);
      }
    }
  }
}
