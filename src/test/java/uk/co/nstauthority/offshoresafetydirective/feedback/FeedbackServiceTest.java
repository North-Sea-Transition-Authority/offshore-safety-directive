package uk.co.nstauthority.offshoresafetydirective.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.co.fivium.feedbackmanagementservice.client.CannotSendFeedbackException;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackClientService;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
class FeedbackServiceTest {
  private static final String CONTEXT_PATH = "/service-name";
  private static final Instant CURRENT_INSTANT = Instant.now();
  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final Nomination NOMINATION = NominationTestUtil.builder().build();
  private static String expectedNominationUrl;

  @Mock
  private Clock clock;

  @Mock
  private FeedbackClientService feedbackClientService;

  @InjectMocks
  private FeedbackService feedbackService;

  @Captor
  private ArgumentCaptor<Feedback> feedbackArgumentCaptor;

  private FeedbackForm form;

  @BeforeAll
  static void setUp() {
    var request = new MockHttpServletRequest();
    request.setContextPath(CONTEXT_PATH);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    expectedNominationUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString() +
        ReverseRouter.route(on(NominationCaseProcessingController.class)
            .renderCaseProcessing(new NominationId(NOMINATION.getId()), null));
  }

  @BeforeEach
  void setup() {
    form = FeedbackFormTestUtil.builder().build();
  }

  @Test
  void saveFeedback_CannotSendFeedbackException_AssertDoesNotThrow() throws CannotSendFeedbackException {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    when(feedbackClientService.saveFeedback(any(Feedback.class)))
        .thenThrow(new CannotSendFeedbackException("test exception"));

    feedbackService.saveFeedback(form, USER);

    verify(feedbackClientService).saveFeedback(feedbackArgumentCaptor.capture());

    assertThat(feedbackArgumentCaptor.getValue()).extracting(
        Feedback::getSubmitterName,
        Feedback::getSubmitterEmail,
        Feedback::getServiceRating,
        Feedback::getComment,
        Feedback::getGivenDatetime,
        Feedback::getTransactionId,
        Feedback::getTransactionReference,
        Feedback::getTransactionLink
    ).containsExactly(
        USER.displayName(),
        USER.emailAddress(),
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        CURRENT_INSTANT,
        null,
        null,
        null
    );
  }

  @Test
  void saveFeedback_canSendFeedback_thenSendToClient() throws CannotSendFeedbackException {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);

    assertDoesNotThrow(() -> feedbackService.saveFeedback(form, USER));

    verify(feedbackClientService).saveFeedback(feedbackArgumentCaptor.capture());

    assertThat(feedbackArgumentCaptor.getValue()).extracting(
        Feedback::getSubmitterName,
        Feedback::getSubmitterEmail,
        Feedback::getServiceRating,
        Feedback::getComment,
        Feedback::getGivenDatetime,
        Feedback::getTransactionId,
        Feedback::getTransactionReference,
        Feedback::getTransactionLink
    ).containsExactly(
        USER.displayName(),
        USER.emailAddress(),
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        CURRENT_INSTANT,
        null,
        null,
        null
    );
  }

  @Test
  void saveFeedback_WithNominationDetail_AssertDoesNotThrow() throws CannotSendFeedbackException {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);

    assertDoesNotThrow(() -> feedbackService.saveFeedback(NOMINATION, form, USER));

    verify(feedbackClientService).saveFeedback(feedbackArgumentCaptor.capture());

    assertThat(feedbackArgumentCaptor.getValue()).extracting(
        Feedback::getSubmitterName,
        Feedback::getSubmitterEmail,
        Feedback::getServiceRating,
        Feedback::getComment,
        Feedback::getGivenDatetime,
        Feedback::getTransactionId,
        Feedback::getTransactionReference,
        Feedback::getTransactionLink
    ).containsExactly(
        USER.displayName(),
        USER.emailAddress(),
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        CURRENT_INSTANT,
        NOMINATION.getId().toString(),
        NOMINATION.getReference(),
        expectedNominationUrl
    );
  }

  @Test
  void saveFeedback_WithNominationDetail_CannotSendFeedbackException_AssertDoesNotThrow() throws CannotSendFeedbackException {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    when(feedbackClientService.saveFeedback(any(Feedback.class)))
        .thenThrow(new CannotSendFeedbackException("test exception"));

    feedbackService.saveFeedback(NOMINATION, form, USER);

    verify(feedbackClientService).saveFeedback(feedbackArgumentCaptor.capture());

    assertThat(feedbackArgumentCaptor.getValue()).extracting(
        Feedback::getSubmitterName,
        Feedback::getSubmitterEmail,
        Feedback::getServiceRating,
        Feedback::getComment,
        Feedback::getGivenDatetime,
        Feedback::getTransactionId,
        Feedback::getTransactionReference,
        Feedback::getTransactionLink
    ).containsExactly(
        USER.displayName(),
        USER.emailAddress(),
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        CURRENT_INSTANT,
        NOMINATION.getId().toString(),
        NOMINATION.getReference(),
        expectedNominationUrl
    );
  }
}