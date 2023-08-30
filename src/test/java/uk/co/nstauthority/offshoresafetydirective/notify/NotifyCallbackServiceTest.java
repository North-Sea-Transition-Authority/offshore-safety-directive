package uk.co.nstauthority.offshoresafetydirective.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.notify.library.FiviumNotificationClientApi;
import uk.co.fivium.notify.library.model.NotifyCallback;
import uk.co.fivium.notify.library.service.FiviumNotifyCallbackService;
import uk.co.nstauthority.offshoresafetydirective.configuration.EmailConfiguration;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
class NotifyCallbackServiceTest {

  @Mock
  private FiviumNotifyCallbackService fiviumNotifyCallbackService;

  @Mock
  private NotifyEmailService notifyEmailService;

  @Mock
  private FiviumNotificationClientApi fiviumNotificationClientApi;

  @Mock
  private NotifyEmailBuilderService notifyEmailBuilderService;
  private NotifyCallbackService notifyCallbackService;
  private final EmailConfiguration emailConfig = new EmailConfiguration("test", "",
      NotifyCallbackTestUtil.BOUNCE_BACK_EMAIL_BOX);

  @BeforeEach
  void setUp() {
    notifyCallbackService = new NotifyCallbackService(fiviumNotifyCallbackService, notifyEmailService,
        fiviumNotificationClientApi, emailConfig, notifyEmailBuilderService);
  }

  @ParameterizedTest
  @EnumSource(value = NotifyCallback.NotifyCallbackStatus.class, names = {"PERMANENT_FAILURE", "TEMPORARY_FAILURE"}, mode = EnumSource.Mode.EXCLUDE)
  void handleNotifyCallback_WrongNotifyCallbackStatus_DoesNotSendEmailToCallbackEmail(
      NotifyCallback.NotifyCallbackStatus notifyCallbackStatus) {
    var notifyCallback = NotifyCallbackTestUtil.createNotifyCallback("test@email.co.uk", notifyCallbackStatus);

    notifyCallbackService.handleNotifyCallback(notifyCallback);

    verify(notifyEmailService, never()).sendEmail(any(), any());
  }

  @ParameterizedTest
  @EnumSource(value = NotifyCallback.NotifyCallbackStatus.class, names = {"PERMANENT_FAILURE", "TEMPORARY_FAILURE", "TECHNICAL_FAILURE"})
  void handleNotifyCallback_EmailToCallbackEmailFailed_DoesNotResendEmailToCallbackEmail(
      NotifyCallback.NotifyCallbackStatus status
  ) {
    var notifyCallback = NotifyCallbackTestUtil.createNotifyCallback(
        NotifyCallbackTestUtil.BOUNCE_BACK_EMAIL_BOX,
        status
    );

    notifyCallbackService.handleNotifyCallback(notifyCallback);

    verify(notifyEmailService, never()).sendEmail(any(), any());
  }

  @ParameterizedTest
  @EnumSource(
      value = NotifyCallback.NotifyCallbackStatus.class,
      names = {"TEMPORARY_FAILURE", "PERMANENT_FAILURE"},
      mode = EnumSource.Mode.INCLUDE
  )
  void handleNotifyCallback_SendsNotificationOfFailedEmailToCallbackEmail(NotifyCallback.NotifyCallbackStatus notifyDeliveryFailureStatus) throws NotificationClientException {
    var emailAddress = "test@email.co.uk";
    var subject = "subject";
    var body = "body";

    var notifyCallback = NotifyCallbackTestUtil.createNotifyCallback(
        emailAddress,
        notifyDeliveryFailureStatus
    );

    var notification = mock(Notification.class);
    when(notification.getEmailAddress()).thenReturn(Optional.of(emailAddress));
    when(notification.getSubject()).thenReturn(Optional.of(subject));
    when(notification.getBody()).thenReturn(body);
    when(fiviumNotificationClientApi.getNotificationById(notifyCallback.getId())).thenReturn(notification);

    var builtEmail = NotifyEmail.builder(NotifyTemplate.EMAIL_DELIVERY_FAILED,
        NotifyEmailTestUtil.serviceBrandingConfigurationProperties);
    when(notifyEmailBuilderService.builder(NotifyTemplate.EMAIL_DELIVERY_FAILED)).thenReturn(builtEmail);

    var argumentCaptor = ArgumentCaptor.forClass(NotifyEmail.class);
    notifyCallbackService.handleNotifyCallback(notifyCallback);

    verify(notifyEmailService).sendEmail(argumentCaptor.capture(), eq(NotifyCallbackTestUtil.BOUNCE_BACK_EMAIL_BOX));
    assertThat(argumentCaptor.getValue())
        .extracting(NotifyEmail::getTemplate)
        .isEqualTo(NotifyTemplate.EMAIL_DELIVERY_FAILED);
    assertThat(argumentCaptor.getValue())
        .extracting(NotifyEmail::getPersonalisations)
        .asInstanceOf(InstanceOfAssertFactories.map(String.class, String.class))
        .contains(
            Map.entry("FAILED_EMAIL_ADDRESS", emailAddress),
            Map.entry("EMAIL_SUBJECT", subject),
            Map.entry("EMAIL_BODY", body)
        );
  }
}