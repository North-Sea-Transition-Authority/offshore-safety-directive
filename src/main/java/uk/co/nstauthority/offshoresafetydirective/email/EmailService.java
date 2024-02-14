package uk.co.nstauthority.offshoresafetydirective.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.NotificationLibraryClient;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailNotification;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;

@Service
public class EmailService {

  public static final String RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME = "RECIPIENT_IDENTIFIER";

  private final NotificationLibraryClient notificationLibraryClient;

  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;

  private final EmailUrlGenerationService emailUrlGenerationService;

  @Autowired
  public EmailService(NotificationLibraryClient notificationLibraryClient,
                      ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
                      EmailUrlGenerationService emailUrlGenerationService) {
    this.notificationLibraryClient = notificationLibraryClient;
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
    this.emailUrlGenerationService = emailUrlGenerationService;
  }

  public MergedTemplate.MergedTemplateBuilder getTemplate(GovukNotifyTemplate notifyTemplate) {

    var serviceBranding = serviceBrandingConfigurationProperties.getServiceConfigurationProperties();
    var customerBranding = serviceBrandingConfigurationProperties.getCustomerConfigurationProperties();

    var subjectPrefix = notificationLibraryClient.isRunningTestMode() ? "***TEST***" : "";

    return notificationLibraryClient.getTemplate(notifyTemplate.getTemplateId())
        .withMailMergeField("SUBJECT_PREFIX", subjectPrefix)
        .withMailMergeField("SERVICE_FULL_NAME", serviceBranding.name())
        .withMailMergeField("SERVICE_MNEMONIC", serviceBranding.mnemonic())
        .withMailMergeField("REGULATION_NAME_SHORT", serviceBranding.regulationNameShort())
        .withMailMergeField("REGULATION_NAME_LONG", serviceBranding.regulationNameLong())
        .withMailMergeField("REGULATOR_BUSINESS_EMAIL_ADDRESS", customerBranding.businessEmailAddress())
        .withMailMergeField("SALUTATION", "Dear")
        .withMailMergeField("VALEDICTION", "Kind regards")
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, "%s user".formatted(serviceBranding.mnemonic()));
  }

  public EmailNotification sendEmail(MergedTemplate mergedTemplate,
                                     EmailRecipient recipient,
                                     DomainReference domainReference) {
    return notificationLibraryClient.sendEmail(
        mergedTemplate,
        recipient,
        domainReference,
        CorrelationIdUtil.getCorrelationIdFromMdc()
    );
  }

  public String withUrl(String url) {
    return emailUrlGenerationService.generateEmailUrl(url);
  }
}
