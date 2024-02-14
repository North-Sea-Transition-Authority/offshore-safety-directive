package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailNotification;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;

@Component
class InstallationRegulatorNotificationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstallationRegulatorNotificationEventListener.class);

  private final EmailService emailService;

  private final NominationEmailBuilderService nominationEmailBuilderService;

  private final AccidentRegulatorConfigurationProperties accidentRegulatorProperties;

  InstallationRegulatorNotificationEventListener(EmailService emailService,
                                                 NominationEmailBuilderService nominationEmailBuilderService,
                                                 AccidentRegulatorConfigurationProperties accidentRegulatorProperties) {
    this.emailService = emailService;
    this.nominationEmailBuilderService = nominationEmailBuilderService;
    this.accidentRegulatorProperties = accidentRegulatorProperties;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyInstallationRegulatorOfConsultation(ConsultationRequestedEvent consultationRequestedEvent) {

    NominationId nominationId = consultationRequestedEvent.getNominationId();

    LOGGER.info("Handling ConsultationRequestedEvent for nomination with ID {}", nominationId.id());

    MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
        .buildConsultationRequestedTemplate(nominationId);

    EmailNotification sentEmail = emailAccidentRegulator(nominationId, templateBuilder);

    LOGGER.info(
        "Sent consultation requested email with ID %s to accident regulator for nomination %s"
            .formatted(sentEmail.id(), nominationId.id())
    );
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyInstallationRegulatorOfNominationDecision(NominationDecisionDeterminedEvent decisionDeterminedEvent) {

    NominationId nominationId = decisionDeterminedEvent.getNominationId();

    LOGGER.info("Handling NominationDecisionDeterminedEvent for nomination with ID {}", nominationId.id());

    MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
        .buildNominationDecisionTemplate(nominationId);

    EmailNotification sentEmail = emailAccidentRegulator(nominationId, templateBuilder);

    LOGGER.info(
        "Sent nomination decision email with ID %s to accident regulator for nomination %s"
            .formatted(sentEmail.id(), nominationId.id())
    );
  }

  private EmailNotification emailAccidentRegulator(NominationId nominationId,
                                                   MergedTemplate.MergedTemplateBuilder mergedTemplateBuilder) {

    var nominationSummaryUrl = ReverseRouter
        .route(on(NominationConsulteeViewController.class)
            .renderNominationView(nominationId));

    MergedTemplate template = mergedTemplateBuilder
        .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, accidentRegulatorProperties.name())
        .withMailMergeField("NOMINATION_LINK", emailService.withUrl(nominationSummaryUrl))
        .merge();

    return emailService.sendEmail(
        template,
        EmailRecipient.directEmailAddress(accidentRegulatorProperties.emailAddress()),
        EmailService.withNominationDomain(nominationId)
    );
  }
}
