package uk.co.nstauthority.offshoresafetydirective.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.NotificationLibraryClient;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.TemplateType;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

class EmailServiceTest {

  private static final GovukNotifyTemplate GOVUK_NOTIFY_TEMPLATE = GovukNotifyTemplate.CONSULTATION_REQUESTED;

   private static final Template TEMPLATE = new Template(
      GOVUK_NOTIFY_TEMPLATE.getTemplateId(),
       TemplateType.EMAIL,
       Set.of(),
       Template.VerificationStatus.CONFIRMED_NOTIFY_TEMPLATE
   );

  private static final ServiceBrandingConfigurationProperties BRANDING_PROPERTIES
      = ServiceBrandingConfigurationPropertiesTestUtil.builder().build();

  private static NotificationLibraryClient notificationLibraryClient;

  private static EmailUrlGenerationService emailUrlGenerationService;

  private static EmailService emailService;

  @BeforeAll
  static void setup() {

    notificationLibraryClient = mock(NotificationLibraryClient.class);

    emailUrlGenerationService = mock(EmailUrlGenerationService.class);

    emailService = new EmailService(
        notificationLibraryClient,
        BRANDING_PROPERTIES,
        emailUrlGenerationService
    );
  }

  @DisplayName("GIVEN I want to get a template")
  @Nested
  class GetTemplate {

    @BeforeAll
    static void setup() {
      given(notificationLibraryClient.getTemplate(GOVUK_NOTIFY_TEMPLATE.getTemplateId()))
          .willReturn(TEMPLATE);
    }

    @DisplayName("WHEN I am in test mode")
    @Nested
    class WhenInTestMode {

      @BeforeAll
      static void setup() {
        given(notificationLibraryClient.isRunningTestMode())
            .willReturn(true);
      }

      @DisplayName("THEN a not blank test email subject prefix will be included as a mail merge field")
      @Test
      void whenInTestMode() {

        var resultingTemplate = emailService
            .getTemplate(GOVUK_NOTIFY_TEMPLATE)
            .merge();

        var serviceBranding = BRANDING_PROPERTIES.getServiceConfigurationProperties();
        var customerBranding = BRANDING_PROPERTIES.getCustomerConfigurationProperties();

        assertThat(resultingTemplate.getMailMergeFields())
            .extracting(MailMergeField::name, MailMergeField::value)
            .containsExactlyInAnyOrder(
                tuple("SUBJECT_PREFIX", "***TEST***"),
                tuple("SERVICE_FULL_NAME", serviceBranding.name()),
                tuple("SERVICE_MNEMONIC", serviceBranding.mnemonic()),
                tuple("REGULATION_NAME_SHORT", serviceBranding.regulationNameShort()),
                tuple("REGULATION_NAME_LONG", serviceBranding.regulationNameLong()),
                tuple("REGULATOR_BUSINESS_EMAIL_ADDRESS", customerBranding.businessEmailAddress()),
                tuple("SALUTATION", "Dear"),
                tuple("VALEDICTION", "Kind regards"),
                tuple("RECIPIENT_IDENTIFIER", "%s user".formatted(serviceBranding.mnemonic()))
            );
      }
    }

    @DisplayName("WHEN I am in production mode")
    @Nested
    class WhenInProductionMode {

      @BeforeAll
      static void setup() {
        given(notificationLibraryClient.isRunningProductionMode())
            .willReturn(true);
      }

      @DisplayName("THEN a blank string test email subject prefix will be included as a mail merge field")
      @Test
      void whenInProductionMode() {

        var resultingTemplate = emailService
            .getTemplate(GOVUK_NOTIFY_TEMPLATE)
            .merge();

        var serviceBranding = BRANDING_PROPERTIES.getServiceConfigurationProperties();
        var customerBranding = BRANDING_PROPERTIES.getCustomerConfigurationProperties();

        assertThat(resultingTemplate.getMailMergeFields())
            .extracting(MailMergeField::name, MailMergeField::value)
            .containsExactlyInAnyOrder(
                tuple("SUBJECT_PREFIX", ""),
                tuple("SERVICE_FULL_NAME", serviceBranding.name()),
                tuple("SERVICE_MNEMONIC", serviceBranding.mnemonic()),
                tuple("REGULATION_NAME_SHORT", serviceBranding.regulationNameShort()),
                tuple("REGULATION_NAME_LONG", serviceBranding.regulationNameLong()),
                tuple("REGULATOR_BUSINESS_EMAIL_ADDRESS", customerBranding.businessEmailAddress()),
                tuple("SALUTATION", "Dear"),
                tuple("VALEDICTION", "Kind regards"),
                tuple("RECIPIENT_IDENTIFIER", "%s user".formatted(serviceBranding.mnemonic()))
            );
      }
    }
  }

  @Test
  void sendEmail() {

    MergedTemplate mergedTemplate = MergedTemplate.builder(TEMPLATE).merge();

    CorrelationIdTestUtil.setCorrelationIdOnMdc("log-correlation-id");

    emailService.sendEmail(
        mergedTemplate,
        EmailRecipient.directEmailAddress("someone@example.com"),
        DomainReference.from("id", "type")
    );

    then(notificationLibraryClient)
        .should()
        .sendEmail(
            refEq(mergedTemplate),
            refEq(EmailRecipient.directEmailAddress("someone@example.com")),
            refEq(DomainReference.from("id", "type")),
            eq("log-correlation-id")
        );
  }

  @Test
  void withNominationDomain() {

    var nominationId = new NominationId(UUID.randomUUID());

    var resultingDomainReference = EmailService.withNominationDomain(nominationId);

    assertThat(resultingDomainReference)
        .extracting(DomainReference::getId, DomainReference::getType)
        .containsExactly(nominationId.id().toString(), "NOMINATION");
  }

  @Test
  void withUrl() {

    given(emailUrlGenerationService.generateEmailUrl("/url"))
        .willReturn("https://wios.co.uk/context/url");

    assertThat(emailService.withUrl("/url")).isEqualTo("https://wios.co.uk/context/url");
  }
}
