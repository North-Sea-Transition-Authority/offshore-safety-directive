package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective.nomination.consultee",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ConsulteeNotificationEventListenerTest {

  @ArchTest
  final ArchRule handleConsultationRequestEvent_isAsync = methods()
      .that()
      .areDeclaredIn(ConsulteeNotificationEventListener.class)
      .and().haveName("handleConsultationRequestEvent")
      .should()
      .beAnnotatedWith(Async.class);

  @ArchTest
  final ArchRule handleConsultationRequestEvent_isTransactionalAfterCommit = methods()
      .that()
      .areDeclaredIn(ConsulteeNotificationEventListener.class)
      .and().haveName("handleConsultationRequestEvent")
      .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));
}