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
class ConsulteeNotificationEventListenerTest {

  @ArchTest
  final ArchRule notifyConsulteeCoordinatorOfConsultation_isAsync = methods()
      .that()
      .areDeclaredIn(ConsulteeNotificationEventListener.class)
      .and().haveName("notifyConsulteeCoordinatorOfConsultation")
      .should()
      .beAnnotatedWith(Async.class);

  @ArchTest
  final ArchRule notifyConsulteeCoordinatorOfConsultation_isTransactionalAfterCommit = methods()
      .that()
      .areDeclaredIn(ConsulteeNotificationEventListener.class)
      .and().haveName("notifyConsulteeCoordinatorOfConsultation")
      .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

  @ArchTest
  final ArchRule notifyConsultationCoordinatorsOfDecision_isAsync = methods()
      .that()
      .areDeclaredIn(ConsulteeNotificationEventListener.class)
      .and().haveName("notifyConsultationCoordinatorsOfDecision")
      .should()
      .beAnnotatedWith(Async.class);

  @ArchTest
  final ArchRule notifyConsultationCoordinatorsOfDecision_isTransactionalAfterCommit = methods()
      .that()
      .areDeclaredIn(ConsulteeNotificationEventListener.class)
      .and().haveName("notifyConsultationCoordinatorsOfDecision")
      .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

  @ArchTest
  final ArchRule notifyConsultationCoordinatorsOfAppointment_isAsync = methods()
      .that()
      .areDeclaredIn(ConsulteeNotificationEventListener.class)
      .and().haveName("notifyConsultationCoordinatorsOfAppointment")
      .should()
      .beAnnotatedWith(Async.class);

  @ArchTest
  final ArchRule notifyConsultationCoordinatorsOfAppointment_isTransactionalAfterCommit = methods()
      .that()
      .areDeclaredIn(ConsulteeNotificationEventListener.class)
      .and().haveName("notifyConsultationCoordinatorsOfAppointment")
      .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));
}