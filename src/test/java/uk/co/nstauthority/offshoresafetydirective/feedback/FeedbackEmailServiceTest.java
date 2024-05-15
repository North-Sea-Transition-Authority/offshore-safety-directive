package uk.co.nstauthority.offshoresafetydirective.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.offshoresafetydirective.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;

@ExtendWith(MockitoExtension.class)
class FeedbackEmailServiceTest {

  private static final TechnicalSupportConfigurationProperties TECHNICAL_SUPPORT_CONFIGURATION_PROPERTIES =
      TechnicalSupportConfigurationPropertiesTestUtil.builder().build();
  private static final String SUBMITTER_NAME = "submitter name";
  private static final String SUBMITTER_EMAIL = "submitter@wios.co.uk";
  private static final String SERVICE_RATING = ServiceFeedbackRating.NEITHER.getFormValue();
  private static final Instant DATE_TIME = Instant.now();
  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private EmailService emailService;

  private FeedbackEmailService feedbackEmailService;

  @Captor
  private ArgumentCaptor<MergedTemplate> mergedTemplateArgumentCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientArgumentCaptor;

  @Captor
  private ArgumentCaptor<DomainReference> domainReferenceArgumentCaptor;

  @BeforeEach
  void setUp() {
    feedbackEmailService = new FeedbackEmailService(emailService, TECHNICAL_SUPPORT_CONFIGURATION_PROPERTIES);
  }

  @Test
  void sendFeedbackFailedToSendEmail() {
    var feedback = getFilledFeedback();

    when(emailService.getTemplate(GovukNotifyTemplate.FEEDBACK_FAILED_TO_SEND))
        .thenReturn(MergedTemplate.builder(new Template(null, null, new HashSet<>(), null)));

    feedbackEmailService.sendFeedbackFailedToSendEmail(feedback, USER);

    verify(emailService).sendEmail(
        mergedTemplateArgumentCaptor.capture(),
        emailRecipientArgumentCaptor.capture(),
        domainReferenceArgumentCaptor.capture()
    );

    assertThat(mergedTemplateArgumentCaptor.getValue().getMailMergeFields())
        .extracting(
            MailMergeField::name,
            MailMergeField::value
        )
        .containsExactlyInAnyOrder(

            tuple(
                RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME,
                TECHNICAL_SUPPORT_CONFIGURATION_PROPERTIES.name()
            ),

            tuple("DATE_TIME", DateUtil.formatLongDateTime(DATE_TIME)),

            tuple("SUBMITTER_NAME", SUBMITTER_NAME),

            tuple("SUBMITTER_EMAIL", SUBMITTER_EMAIL),

            tuple("SERVICE_RATING", SERVICE_RATING),

            tuple("SERVICE_IMPROVEMENT", "No comment provided"),

            tuple("TRANSACTION_DETAILS", "Not related to a transaction")
        );
  }

  @Test
  void sendFeedbackFailedToSendEmail_whenTransactionInformationPresent() {
    var feedback = getFilledFeedback();
    feedback.setTransactionId("id-%s".formatted(UUID.randomUUID()));
    feedback.setTransactionLink("/transaction/link");
    feedback.setTransactionReference("ref-%s".formatted(UUID.randomUUID()));

    when(emailService.getTemplate(GovukNotifyTemplate.FEEDBACK_FAILED_TO_SEND))
        .thenReturn(MergedTemplate.builder(new Template(null, null, new HashSet<>(), null)));

    feedbackEmailService.sendFeedbackFailedToSendEmail(feedback, USER);

    verify(emailService).sendEmail(
        mergedTemplateArgumentCaptor.capture(),
        emailRecipientArgumentCaptor.capture(),
        domainReferenceArgumentCaptor.capture()
    );

    assertThat(mergedTemplateArgumentCaptor.getValue().getMailMergeFields())
        .extracting(
            MailMergeField::name,
            MailMergeField::value
        )
        .containsExactlyInAnyOrder(
            tuple(
                RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, TECHNICAL_SUPPORT_CONFIGURATION_PROPERTIES.name()
            ),
            tuple("DATE_TIME", DateUtil.formatLongDateTime(DATE_TIME)),
            tuple("SUBMITTER_NAME", SUBMITTER_NAME),
            tuple("SUBMITTER_EMAIL", SUBMITTER_EMAIL),
            tuple("SERVICE_RATING", SERVICE_RATING),
            tuple("SERVICE_IMPROVEMENT", "No comment provided"),
            tuple(
                "TRANSACTION_DETAILS",
                """
                Transaction ID: %s
                Transaction reference: %s
                Transaction link: %s
                """
                    .formatted(
                        feedback.getTransactionId(),
                        feedback.getTransactionReference(),
                        feedback.getTransactionLink()
                    )
            )
        );
  }

  @Test
  void sendFeedbackFailedToSendEmail_whenServiceImprovementPresent() {
    var feedback = getFilledFeedback();
    feedback.setComment("comment");

    when(emailService.getTemplate(GovukNotifyTemplate.FEEDBACK_FAILED_TO_SEND))
        .thenReturn(MergedTemplate.builder(new Template(null, null, new HashSet<>(), null)));

    feedbackEmailService.sendFeedbackFailedToSendEmail(feedback, USER);

    verify(emailService).sendEmail(
        mergedTemplateArgumentCaptor.capture(),
        emailRecipientArgumentCaptor.capture(),
        domainReferenceArgumentCaptor.capture()
    );

    assertThat(mergedTemplateArgumentCaptor.getValue().getMailMergeFields())
        .extracting(
            MailMergeField::name,
            MailMergeField::value
        )
        .containsExactlyInAnyOrder(
            tuple(
                RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, TECHNICAL_SUPPORT_CONFIGURATION_PROPERTIES.name()
            ),
            tuple("DATE_TIME", DateUtil.formatLongDateTime(DATE_TIME)),
            tuple("SUBMITTER_NAME", SUBMITTER_NAME),
            tuple("SUBMITTER_EMAIL", SUBMITTER_EMAIL),
            tuple("SERVICE_RATING", SERVICE_RATING),
            tuple("SERVICE_IMPROVEMENT", feedback.getComment()),
            tuple("TRANSACTION_DETAILS", "Not related to a transaction")
        );
  }

  private Feedback getFilledFeedback() {
    var feedback = new Feedback();
    feedback.setServiceRating(SERVICE_RATING);
    feedback.setGivenDatetime(DATE_TIME);
    feedback.setSubmitterEmail(SUBMITTER_EMAIL);
    feedback.setSubmitterName(SUBMITTER_NAME);
    return feedback;
  }
}