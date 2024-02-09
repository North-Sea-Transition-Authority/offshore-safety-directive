package uk.co.nstauthority.offshoresafetydirective.feedback;

import static uk.co.nstauthority.offshoresafetydirective.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;

@Service
class FeedbackEmailService {

  private final EmailService emailService;
  private final TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties;

  @Autowired
  FeedbackEmailService(EmailService emailService,
                       TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties) {
    this.emailService = emailService;
    this.technicalSupportConfigurationProperties = technicalSupportConfigurationProperties;
  }

  void sendFeedbackFailedToSendEmail(Feedback feedback, ServiceUserDetail userDetail) {
    var template = emailService.getTemplate(GovukNotifyTemplate.FEEDBACK_FAILED_TO_SEND)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, technicalSupportConfigurationProperties.name())
        .withMailMergeField("SUBMITTER_NAME", feedback.getSubmitterName())
        .withMailMergeField("SUBMITTER_EMAIL", feedback.getSubmitterEmail())
        .withMailMergeField("SERVICE_RATING", feedback.getServiceRating())
        .withMailMergeField("DATE_TIME", DateUtil.formatLongDateTime(feedback.getGivenDatetime()))
        .withMailMergeField("TRANSACTION_DETAILS", getTransactionDetails(feedback))
        .withMailMergeField("SERVICE_IMPROVEMENT", getServiceImprovement(feedback))
        .merge();

    var domainReference = DomainReference.from(userDetail.wuaId().toString(), "USER_FEEDBACK");

    emailService.sendEmail(
        template,
        EmailRecipient.directEmailAddress(technicalSupportConfigurationProperties.emailAddress()),
        domainReference
    );
  }

  private String getTransactionDetails(Feedback feedback) {
    if (StringUtils.isBlank(feedback.getTransactionId())) {
      return "Not related to a transaction";
    } else {
      return """
          Transaction ID: %s
          Transaction reference: %s
          Transaction link: %s
          """.formatted(
          feedback.getTransactionId(),
          feedback.getTransactionReference(),
          feedback.getTransactionLink()
      );
    }
  }

  private String getServiceImprovement(Feedback feedback) {
    if (StringUtils.isBlank(feedback.getComment())) {
      return "No comment provided";
    } else {
      return feedback.getComment();
    }
  }
}
