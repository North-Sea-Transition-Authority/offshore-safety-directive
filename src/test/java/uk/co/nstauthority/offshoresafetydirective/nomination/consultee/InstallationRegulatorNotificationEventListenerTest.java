package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import uk.co.nstauthority.offshoresafetydirective.branding.InstallationRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.InstallationRegulatorPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyTemplate;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective.nomination.consultee",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@ExtendWith(MockitoExtension.class)
class InstallationRegulatorNotificationEventListenerTest {

  private static ConsulteeEmailCreationService consulteeEmailCreationService;

  private static InstallationRegulatorConfigurationProperties installationRegulator;

  private static final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties
      = NotifyEmailTestUtil.serviceBrandingConfigurationProperties;

  private static NotifyEmailService notifyEmailService;

  private static InstallationRegulatorNotificationEventListener installationRegulatorNotificationEventListener;

  @BeforeEach
  void setup() {
    consulteeEmailCreationService = mock(ConsulteeEmailCreationService.class);
    notifyEmailService = mock(NotifyEmailService.class);

    installationRegulator = InstallationRegulatorPropertiesTestUtil.builder()
        .withName("name")
        .withMnemonic("mnemonic")
        .withEmail("email")
        .build();

    installationRegulatorNotificationEventListener = new InstallationRegulatorNotificationEventListener(
        consulteeEmailCreationService, installationRegulator, notifyEmailService);
  }

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
  void notifyInstallationRegulatorOfConsultation_whenInstallationRegulators_thenEmailSent() {
    var nominationId = new NominationId(100);
    var event = new ConsultationRequestedEvent(nominationId);

    var notifyEmail = NotifyEmail.builder(NotifyTemplate.CONSULTATION_REQUESTED, serviceBrandingConfigurationProperties).build();

    given(consulteeEmailCreationService.constructConsultationRequestEmail(nominationId, installationRegulator.name()))
        .willReturn(notifyEmail);

    installationRegulatorNotificationEventListener.notifyInstallationRegulatorOfConsultation(event);

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmail, installationRegulator.email());

    then(notifyEmailService)
        .shouldHaveNoMoreInteractions();
  }

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
  void notifyInstallationRegulatorOfNominationDecision_whenInstallationRegulators_thenEmailSent() {
    var nominationId = new NominationId(100);
    var event = new NominationDecisionDeterminedEvent(nominationId);

    var notifyEmail = NotifyEmail.builder(NotifyTemplate.NOMINATION_DECISION_DETERMINED, serviceBrandingConfigurationProperties).build();

    given(consulteeEmailCreationService.constructNominationDecisionDeterminedEmail(nominationId, installationRegulator.name()))
        .willReturn(notifyEmail);

    installationRegulatorNotificationEventListener.notifyInstallationRegulatorOfNominationDecision(event);

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmail, installationRegulator.email());

    then(notifyEmailService)
        .shouldHaveNoMoreInteractions();
  }
}