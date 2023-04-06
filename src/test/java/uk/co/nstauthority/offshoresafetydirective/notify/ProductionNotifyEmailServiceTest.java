package uk.co.nstauthority.offshoresafetydirective.notify;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.notify.library.FiviumNotificationClientApi;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

@ExtendWith(MockitoExtension.class)
class ProductionNotifyEmailServiceTest {

  @Mock
  private FiviumNotificationClientApi fiviumNotificationClientApi;
  @Mock
  private NotifyEmailValidator notifyEmailValidator;

  private ProductionNotifyEmailService productionNotifyEmailService;

  @BeforeEach
  void setUp() {
    productionNotifyEmailService = new ProductionNotifyEmailService(fiviumNotificationClientApi,
        notifyEmailValidator);
  }

  @Test
  void sendEmail_EmailNotValid_ReturnsEarly() {
    var toEmailAddress = "toEmailAddress";
    var reference = "reference";
    var emailReplyToId = "emailReplyToId";
    var notifyTemplate = NotifyTemplate.EMAIL_DELIVERY_FAILED;

    when(notifyEmailValidator.isValid(toEmailAddress)).thenReturn(false);

    var notifyEmail = NotifyEmail.builder(
        notifyTemplate,
        NotifyEmailTestUtil.serviceBrandingConfigurationProperties
    ).build();

    productionNotifyEmailService.sendEmail(notifyEmail, toEmailAddress, reference, emailReplyToId);

    verifyNoInteractions(fiviumNotificationClientApi);
  }

  @Test
  void sendEmail_VerifyCalls() throws NotificationClientException {
    var toEmailAddress = "toEmailAddress";
    var reference = "reference";
    var emailReplyToId = "emailReplyToId";

    when(notifyEmailValidator.isValid(toEmailAddress)).thenReturn(true);

    var notifyEmail = NotifyEmail.builder(
        NotifyTemplate.EMAIL_DELIVERY_FAILED,
        NotifyEmailTestUtil.serviceBrandingConfigurationProperties
    ).build();

    var emailResponse = mock(SendEmailResponse.class);
    var responseUuid = UUID.randomUUID();
    when(emailResponse.getNotificationId()).thenReturn(responseUuid);

    when(fiviumNotificationClientApi.sendEmail(notifyEmail.getTemplate().getTemplateName(), toEmailAddress,
        notifyEmail.getPersonalisations(), reference, emailReplyToId))
        .thenReturn(emailResponse);

    productionNotifyEmailService.sendEmail(notifyEmail, toEmailAddress, reference, emailReplyToId);

    assertThat(notifyEmail.getPersonalisations())
        .asInstanceOf(InstanceOfAssertFactories.map(String.class, String.class))
        .containsExactly(
            Map.entry(
                "SERVICE_NAME",
                NotifyEmailTestUtil.serviceBrandingConfigurationProperties.getServiceConfigurationProperties().name()
            ),
            Map.entry("SUBJECT_PREFIX", ""),
            Map.entry(
                "IS_TEST_EMAIL",
                "no"
            )
        );

    verify(fiviumNotificationClientApi).sendEmail(
        notifyEmail.getTemplate().getTemplateName(),
        toEmailAddress,
        notifyEmail.getPersonalisations(),
        reference,
        emailReplyToId
    );
  }

}