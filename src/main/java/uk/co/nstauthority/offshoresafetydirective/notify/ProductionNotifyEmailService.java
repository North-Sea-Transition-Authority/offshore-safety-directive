package uk.co.nstauthority.offshoresafetydirective.notify;

import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.co.fivium.notify.library.FiviumNotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

@Service
@ConditionalOnProperty(name = "email.mode", havingValue = "production")
public class ProductionNotifyEmailService implements NotifyEmailService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProductionNotifyEmailService.class);

  private final FiviumNotificationClientApi fiviumNotificationClientApi;
  private final NotifyEmailValidator notifyEmailValidator;

  public ProductionNotifyEmailService(FiviumNotificationClientApi fiviumNotificationClientApi,
                                      NotifyEmailValidator notifyEmailValidator) {
    this.fiviumNotificationClientApi = fiviumNotificationClientApi;
    this.notifyEmailValidator = notifyEmailValidator;
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

    if (!notifyEmailValidator.isValid(toEmailAddress)) {
      LOGGER.error("Invalid toEmailAddress [{}]", toEmailAddress);
      return;
    }

    var personalisation = notifyEmail.getPersonalisations();

    try {
      var sentEmailResponse = fiviumNotificationClientApi
          .sendEmail(notifyEmail.getTemplate().getTemplateName(), toEmailAddress, personalisation, reference,
              emailReplyToId);

      LOGGER.info(
          "Email with notification ID [{}] was sent to Notify successfully",
          sentEmailResponse.getNotificationId()
      );
    } catch (NotificationClientException e) {
      LOGGER.error("Error constructing NotificationClient when sending email with template [%s]"
              .formatted(notifyEmail.getTemplate().getTemplateName()),
          e
      );
    }
  }
}
