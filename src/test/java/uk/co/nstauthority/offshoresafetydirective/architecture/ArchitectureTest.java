package uk.co.nstauthority.offshoresafetydirective.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import com.tngtech.archunit.lang.ArchRule;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityRule;

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
}
