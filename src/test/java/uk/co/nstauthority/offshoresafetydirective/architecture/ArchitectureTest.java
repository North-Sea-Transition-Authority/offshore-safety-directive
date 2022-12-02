package uk.co.nstauthority.offshoresafetydirective.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityRule;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

  @ArchTest
  final ArchTests securityRules = ArchTests.in(SecurityRule.class);
}
