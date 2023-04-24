package uk.co.nstauthority.offshoresafetydirective.notify;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.notify.library.FiviumNotificationClientApi;
import uk.co.nstauthority.offshoresafetydirective.configuration.EmailConfiguration;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
class TestNotifyEmailServiceTest {
  private final String testEmail1 = "one@test.com";
  private final String testEmail2 = "two@test.com";
  private final EmailConfiguration emailConfig = new EmailConfiguration("test", String.format("%s, %s", testEmail1, testEmail2), "email");
  @Mock
  private FiviumNotificationClientApi fiviumNotificationClientApi;
  @Mock
  private NotifyEmailValidator notifyEmailValidator;

  private TestNotifyEmailService testNotifyEmailService;

  @BeforeEach
  void setUp() {
    testNotifyEmailService = new TestNotifyEmailService(fiviumNotificationClientApi, notifyEmailValidator, emailConfig);
  }

  @Test
  void sendEmail_EmailNotValid_ReturnsEarly() {
    var toEmailAddress = "toEmailAddress";
    var reference = "reference";
    var emailReplyToId = "emailReplyToId";

    when(notifyEmailValidator.isValid(testEmail1)).thenReturn(false);
    when(notifyEmailValidator.isValid(testEmail2)).thenReturn(false);

    var notifyEmail = NotifyEmail.builder(
        NotifyTemplate.EMAIL_DELIVERY_FAILED,
        NotifyEmailTestUtil.serviceBrandingConfigurationProperties
    ).build();

    testNotifyEmailService.sendEmail(notifyEmail, toEmailAddress, reference, emailReplyToId);

    verifyNoInteractions(fiviumNotificationClientApi);
  }

  @Test
  void sendEmail_VerifyCalls() throws NotificationClientException {
    var toEmailAddress = "toEmailAddress";
    var reference = "reference";
    var emailReplyToId = "emailReplyToId";

    when(notifyEmailValidator.isValid(testEmail1)).thenReturn(true);
    when(notifyEmailValidator.isValid(testEmail2)).thenReturn(true);

    var notifyEmail = NotifyEmail.builder(
        NotifyTemplate.EMAIL_DELIVERY_FAILED,
        NotifyEmailTestUtil.serviceBrandingConfigurationProperties
    ).build();

    testNotifyEmailService.sendEmail(notifyEmail, toEmailAddress, reference, emailReplyToId);

    var expectedPersonalisations = NotifyEmailTestUtil.getDefaultEmailProperties(
        NotifyEmailTestUtil.serviceBrandingConfigurationProperties
    );

    expectedPersonalisations.put("SUBJECT_PREFIX", "**TEST EMAIL** ");
    expectedPersonalisations.put("IS_TEST_EMAIL", "yes");

    assertThat(notifyEmail.getPersonalisations())
        .asInstanceOf(InstanceOfAssertFactories.map(String.class, String.class))
        .containsAllEntriesOf(expectedPersonalisations);

    verify(fiviumNotificationClientApi).sendEmail(
        notifyEmail.getTemplate().getTemplateName(),
        testEmail1,
        notifyEmail.getPersonalisations(),
        reference,
        emailReplyToId
    );

    verify(fiviumNotificationClientApi).sendEmail(
        notifyEmail.getTemplate().getTemplateName(),
        testEmail2,
        notifyEmail.getPersonalisations(),
        reference,
        emailReplyToId
    );
  }

}