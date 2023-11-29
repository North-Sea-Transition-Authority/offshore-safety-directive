package uk.co.nstauthority.offshoresafetydirective.authorisation;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public class TestSecurityRules {

  ArchCondition<JavaClass> containAtLeastOneSecurityTest =
      new ArchCondition<>("contain at least one @SecurityTest") {
        @Override
        public void check(JavaClass javaClass, ConditionEvents conditionEvents) {

          var securityTest = javaClass.getAllMethods()
              .stream()
              .filter(javaMethodCall -> javaMethodCall.isAnnotatedWith(SecurityTest.class))
              .findAny();

          if (securityTest.isEmpty()) {
            conditionEvents.add(
                SimpleConditionEvent.violated(javaClass, String.format("%s doesn't contain a @SecurityTest", javaClass.getSimpleName())));
          }
        }
      };

  @ArchTest
  final ArchRule securityTestAnnotationRule = classes()
      .that().haveSimpleNameEndingWith("ControllerTest")
      .and().doNotHaveSimpleName("AbstractControllerTest")
      .and().doNotHaveSimpleName("AbstractNominationControllerTest")
      .and().doNotHaveSimpleName("AbstractActuatorControllerTest")
      .and().doNotHaveSimpleName("DefaultClientErrorControllerTest")
      .should(containAtLeastOneSecurityTest);
}
