package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationSubmittedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationTypeService;
import uk.co.nstauthority.offshoresafetydirective.nomination.operatorinvolvement.NominationOperatorService;
import uk.co.nstauthority.offshoresafetydirective.nomination.operatorinvolvement.NominationOperators;

@Component
class RegulatorNotificationSubmissionEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegulatorNotificationSubmissionEventListener.class);

  private final NominationDetailService nominationDetailService;

  private final CustomerConfigurationProperties customerConfigurationProperties;

  private final NominationOperatorService nominationOperatorService;

  private final EmailService emailService;

  private final NominationTypeService nominationTypeService;

  @Autowired
  RegulatorNotificationSubmissionEventListener(NominationDetailService nominationDetailService,
                                               CustomerConfigurationProperties customerConfigurationProperties,
                                               NominationOperatorService nominationOperatorService,
                                               EmailService emailService,
                                               NominationTypeService nominationTypeService) {
    this.nominationDetailService = nominationDetailService;
    this.customerConfigurationProperties = customerConfigurationProperties;
    this.nominationOperatorService = nominationOperatorService;
    this.emailService = emailService;
    this.nominationTypeService = nominationTypeService;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyRegulatorOfSubmission(NominationSubmittedEvent nominationSubmittedEvent) {

    NominationId nominationId = nominationSubmittedEvent.getNominationId();

    var nominationDetail = getNominationDetail(nominationSubmittedEvent.getNominationId());

    NominationOperators nominationOperators = nominationOperatorService.getNominationOperators(nominationDetail);

    String nominationSummaryPageUrl = ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(nominationId, null));

    MergedTemplate template = emailService.getTemplate(GovukNotifyTemplate.NOMINATION_SUBMITTED)
        .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, customerConfigurationProperties.name())
        .withMailMergeField("NOMINATION_REFERENCE", nominationDetail.getNomination().getReference())
        .withMailMergeField("APPLICANT", nominationOperators.applicant().name())
        .withMailMergeField("NOMINEE", nominationOperators.nominee().name())
        .withMailMergeField("OPERATORSHIP_TYPE", getNominationOperatorshipText(nominationDetail))
        .withMailMergeField("NOMINATION_LINK", emailService.withUrl(nominationSummaryPageUrl))
        .merge();

    var sentEmail = emailService.sendEmail(
        template,
        EmailRecipient.directEmailAddress(customerConfigurationProperties.businessEmailAddress()),
        nominationDetail
    );

    LOGGER.info(
        "Nomination submission notification {} for nomination detail {} sent to regulator inbox",
        sentEmail.id(),
        nominationDetail.getId()
    );
  }

  private NominationDetail getNominationDetail(NominationId nominationId) {

    Optional<NominationDetail> nominationDetail = nominationDetailService
        .getPostSubmissionNominationDetail(nominationId);

    if (nominationDetail.isEmpty()) {
      throw new IllegalStateException(
          "Could not find latest submitted NominationDetail for nomination with ID %s".formatted(nominationId.id())
      );
    }

    return nominationDetail.get();
  }

  String getNominationOperatorshipText(NominationDetail nominationDetail) {
    return switch (nominationTypeService.getNominationDisplayType(nominationDetail)) {
      case WELL_AND_INSTALLATION -> "a well and installation operator";
      case WELL -> "a well operator";
      case INSTALLATION -> "an installation operator";
      case NOT_PROVIDED -> "an operator";
    };
  }
}
