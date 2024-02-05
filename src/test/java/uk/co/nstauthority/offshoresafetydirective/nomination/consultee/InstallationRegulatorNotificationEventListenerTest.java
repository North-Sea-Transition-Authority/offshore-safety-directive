package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailNotification;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective.nomination.consultee",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@ExtendWith(MockitoExtension.class)
class InstallationRegulatorNotificationEventListenerTest {

  private EmailService emailService;

  private ConsulteeEmailBuilderService consulteeEmailBuilderService;

  private AccidentRegulatorConfigurationProperties accidentRegulatorProperties;

  @Captor
  private ArgumentCaptor<MergedTemplate> mergedTemplateArgumentCaptor;

  private InstallationRegulatorNotificationEventListener installationRegulatorNotificationEventListener;

  @BeforeEach
  void setup() {

    emailService = mock(EmailService.class);
    consulteeEmailBuilderService = mock(ConsulteeEmailBuilderService.class);

    accidentRegulatorProperties = new AccidentRegulatorConfigurationProperties(
        "name",
        "mnemonic",
        "guidance-url",
        "email-address"
    );

    installationRegulatorNotificationEventListener = new InstallationRegulatorNotificationEventListener(
        emailService,
        consulteeEmailBuilderService,
        accidentRegulatorProperties
    );

  }

  @Nested
  class NotifyInstallationRegulatorOfConsultation {

    @ArchTest
    final ArchRule notifyInstallationRegulatorOfConsultation_isAsync = methods()
        .that()
        .areDeclaredIn(InstallationRegulatorNotificationEventListener.class)
        .and().haveName("notifyInstallationRegulatorOfConsultation")
        .should()
        .beAnnotatedWith(Async.class);

    @ArchTest
    final ArchRule notifyInstallationRegulatorOfConsultation_isTransactionalAfterCommit = methods()
        .that()
        .areDeclaredIn(InstallationRegulatorNotificationEventListener.class)
        .and().haveName("notifyInstallationRegulatorOfConsultation")
        .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

    @Test
    void thenAccidentRegulatorIsInformed() {

      var nominationId = new NominationId(UUID.randomUUID());

      MergedTemplate.MergedTemplateBuilder expectedTemplate = MergedTemplate
          .builder(new Template(null, null, Set.of(), null));

      given(consulteeEmailBuilderService.buildConsultationRequestedTemplate(nominationId))
          .willReturn(expectedTemplate);

      // to avoid an NPE in the log statement in the code
      given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

      installationRegulatorNotificationEventListener
          .notifyInstallationRegulatorOfConsultation(new ConsultationRequestedEvent(nominationId));

      then(emailService)
          .should()
          .sendEmail(
              mergedTemplateArgumentCaptor.capture(),
              refEq(EmailRecipient.directEmailAddress(accidentRegulatorProperties.emailAddress())),
              refEq(EmailService.withNominationDomain(nominationId))
          );

      Optional<MailMergeField> recipientName = mergedTemplateArgumentCaptor.getValue().getMailMergeFields()
          .stream()
          .filter(mailMergeField -> Objects.equals(mailMergeField.name(), EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME))
          .findFirst();

      assertThat(recipientName).isPresent();
      assertThat(recipientName.get().value()).isEqualTo(accidentRegulatorProperties.name());
    }
  }

  @Nested
  class NotifyInstallationRegulatorOfNominationDecision {

    @ArchTest
    final ArchRule notifyInstallationRegulatorOfNominationDecision_isAsync = methods()
        .that()
        .areDeclaredIn(InstallationRegulatorNotificationEventListener.class)
        .and().haveName("notifyInstallationRegulatorOfNominationDecision")
        .should()
        .beAnnotatedWith(Async.class);

    @ArchTest
    final ArchRule notifyInstallationRegulatorOfNominationDecision_isTransactionalAfterCommit = methods()
        .that()
        .areDeclaredIn(InstallationRegulatorNotificationEventListener.class)
        .and().haveName("notifyInstallationRegulatorOfNominationDecision")
        .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

    @Test
    void thenAccidentRegulatorIsInformed() {

      var nominationId = new NominationId(UUID.randomUUID());

      MergedTemplate.MergedTemplateBuilder expectedTemplate = MergedTemplate
          .builder(new Template(null, null, Set.of(), null));

      given(consulteeEmailBuilderService.buildNominationDecisionTemplate(nominationId))
          .willReturn(expectedTemplate);

      // to avoid an NPE in the log statement in the code
      given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

      installationRegulatorNotificationEventListener
          .notifyInstallationRegulatorOfNominationDecision(new NominationDecisionDeterminedEvent(nominationId));

      then(emailService)
          .should()
          .sendEmail(
              mergedTemplateArgumentCaptor.capture(),
              refEq(EmailRecipient.directEmailAddress(accidentRegulatorProperties.emailAddress())),
              refEq(EmailService.withNominationDomain(nominationId))
          );

      Optional<MailMergeField> recipientName = mergedTemplateArgumentCaptor.getValue().getMailMergeFields()
          .stream()
          .filter(mailMergeField -> Objects.equals(mailMergeField.name(), EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME))
          .findFirst();

      assertThat(recipientName).isPresent();
      assertThat(recipientName.get().value()).isEqualTo(accidentRegulatorProperties.name());
    }
  }
}