package uk.co.nstauthority.offshoresafetydirective.authorisation;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

public class SecurityRule {

  @ArchTest
  final ArchRule securityAnnotationRule = methods()
      .that().areAnnotatedWith(GetMapping.class)
      .or().areAnnotatedWith(PostMapping.class)
      .or().areAnnotatedWith(PutMapping.class)
      .or().areAnnotatedWith(DeleteMapping.class)
      .and().areDeclaredInClassesThat().areNotAnnotatedWith(RestControllerEndpoint.class)
      .should()
        // meta annotated as the annotation is included as part of other annotations
        .beMetaAnnotatedWith(Security.class)
      .andShould()
        // prevent method having this annotation directly as it does nothing on its own
        .notBeAnnotatedWith(Security.class)
      .orShould()
        // check for the annotation on the class level
        .beDeclaredInClassesThat()
        .areMetaAnnotatedWith(Security.class)
      .andShould()
        // check the class doesn't have the annotation directly
        .beDeclaredInClassesThat()
        .areNotAnnotatedWith(Security.class);
}
