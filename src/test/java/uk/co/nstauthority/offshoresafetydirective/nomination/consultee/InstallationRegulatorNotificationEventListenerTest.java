package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective.nomination.consultee",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@ExtendWith(MockitoExtension.class)
class InstallationRegulatorNotificationEventListenerTest {

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
}