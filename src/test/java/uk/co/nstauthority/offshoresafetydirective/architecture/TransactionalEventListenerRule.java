package uk.co.nstauthority.offshoresafetydirective.architecture;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective",
    importOptions = ImportOption.DoNotIncludeTests.class
)
public class TransactionalEventListenerRule {

  private TransactionalEventListenerRule() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static ArchCondition<JavaMethod> haveTransactionalEventListenerWithPhase(TransactionPhase requiredTransactionPhase) {
    return new ArchCondition<>("have annotation TransactionalEventListener with phase %s".formatted(requiredTransactionPhase)) {
      @Override
      public void check(JavaMethod item, ConditionEvents events) {

        try {
          var providedTransactionPhase = item.getAnnotationOfType(TransactionalEventListener.class).phase();

          if (!requiredTransactionPhase.equals(providedTransactionPhase)) {

            var ruleViolationMessage = String.format(
                "Method %s is annotated with @TransactionalEventListener but phase is not set to %s",
                item.getFullName(),
                requiredTransactionPhase
            );
            events.add(SimpleConditionEvent.violated(item, ruleViolationMessage));
          }
        } catch (IllegalArgumentException exception) {

          var ruleViolationMessage = String.format(
              "Method %s is not annotated with @TransactionalEventListener",
              item.getFullName()
          );
          events.add(SimpleConditionEvent.violated(item, ruleViolationMessage));
        }
      }
    };
  }
}
