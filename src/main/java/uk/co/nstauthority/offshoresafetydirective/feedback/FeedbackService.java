package uk.co.nstauthority.offshoresafetydirective.feedback;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.co.fivium.feedbackmanagementservice.client.CannotSendFeedbackException;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackClientService;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;

@Service
class FeedbackService {
  private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackService.class);

  private final FeedbackClientService feedbackClientService;
  private final Clock clock;

  @Autowired
  FeedbackService(FeedbackClientService feedbackClientService,
                  Clock clock) {
    this.feedbackClientService = feedbackClientService;
    this.clock = clock;
  }

  void saveFeedback(FeedbackForm form, ServiceUserDetail userDetail) {
    var feedback = new Feedback();
    sendFeedback(feedback, form, userDetail);
  }

  void saveFeedback(Nomination nomination, FeedbackForm form, ServiceUserDetail userDetail) {
    var feedback = new Feedback();
    feedback.setTransactionId(nomination.getId().toString());
    feedback.setTransactionReference(nomination.getReference());
    feedback.setTransactionLink(getReferenceUrl(new NominationId(nomination.getId())));
    sendFeedback(feedback, form, userDetail);
  }

  void sendFeedback(Feedback feedback, FeedbackForm form, ServiceUserDetail userDetail) {
    feedback.setServiceRating(form.getServiceRating());
    feedback.setComment(form.getFeedback().getInputValue());
    feedback.setGivenDatetime(clock.instant());
    feedback.setSubmitterEmail(userDetail.emailAddress());
    feedback.setSubmitterName(userDetail.displayName());
    try {
      feedbackClientService.saveFeedback(feedback);
    } catch (CannotSendFeedbackException e) {
      LOGGER.warn("Feedback failed to send: {} ", e.getMessage());
    }
  }

  private static String getReferenceUrl(NominationId nominationId) {
    var baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
    var caseProcessingLink = ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(nominationId, null));
    return "%s%s".formatted(baseUrl, caseProcessingLink);
  }
}
