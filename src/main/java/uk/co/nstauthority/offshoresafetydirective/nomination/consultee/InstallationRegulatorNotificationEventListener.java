package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.branding.InstallationRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailService;

@Component
class InstallationRegulatorNotificationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstallationRegulatorNotificationEventListener.class);

  private final ConsulteeEmailCreationService consulteeEmailCreationService;

  private final InstallationRegulatorConfigurationProperties installationRegulator;

  private final NotifyEmailService notifyEmailService;

  @Autowired
  InstallationRegulatorNotificationEventListener(ConsulteeEmailCreationService consulteeEmailCreationService,
                                                 InstallationRegulatorConfigurationProperties installationRegulator,
                                                 NotifyEmailService notifyEmailService) {
    this.consulteeEmailCreationService = consulteeEmailCreationService;
    this.installationRegulator = installationRegulator;
    this.notifyEmailService = notifyEmailService;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyInstallationRegulatorOfConsultation(ConsultationRequestedEvent consultationRequestedEvent) {
    NominationId nominationId = consultationRequestedEvent.getNominationId();

    LOGGER.info("Handling ConsultationRequestedEvent for nomination with ID {}", nominationId.id());

    NotifyEmail.Builder consultationRequestedEmailBuilder = consulteeEmailCreationService
        .constructDefaultConsultationRequestEmail(nominationId);

    emailInstallationRegulator(consultationRequestedEmailBuilder);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyInstallationRegulatorOfNominationDecision(NominationDecisionDeterminedEvent decisionDeterminedEvent) {
    NominationId nominationId = decisionDeterminedEvent.getNominationId();

    LOGGER.info("Handling NominationDecisionDeterminedEvent for nomination with ID {}", nominationId.id());

    NotifyEmail.Builder decisionDeterminedEmailBuilder = consulteeEmailCreationService
        .constructDefaultNominationDecisionDeterminedEmail(nominationId);

    emailInstallationRegulator(decisionDeterminedEmailBuilder);
  }

  private void emailInstallationRegulator(NotifyEmail.Builder emailBuilder) {

    NotifyEmail email = emailBuilder
        .addRecipientIdentifier(installationRegulator.name())
        .build();

    notifyEmailService.sendEmail(email, installationRegulator.email());
  }
}
