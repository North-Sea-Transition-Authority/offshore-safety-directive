package uk.co.nstauthority.offshoresafetydirective.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.theClass;

import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import com.tngtech.archunit.lang.ArchRule;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import uk.co.fivium.digitalenummaterialisationlibrary.enummaterialisation.MaterialisableEnum;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityRule;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.Displayable;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

  @ArchTest
  final ArchTests securityRules = ArchTests.in(SecurityRule.class);

  @ArchTest
  final ArchRule scheduledAnnotationRule = methods()
      .that().areAnnotatedWith(Scheduled.class)
      .should().beAnnotatedWith(SchedulerLock.class);


  @ArchTest
  final ArchRule materialisableEnumRule = classes()
      .that().areEnums()
      .and().containAnyMethodsThat(
      HasName.Predicates.name("getDisplayName").or(HasName.Predicates.name("getDisplayOrder"))
      )
      .should().implement(MaterialisableEnum.class);

  @ArchTest
  final ArchRule displayableRule = theClass(Displayable.class)
      .should().beAssignableTo(MaterialisableEnum.class);

}
