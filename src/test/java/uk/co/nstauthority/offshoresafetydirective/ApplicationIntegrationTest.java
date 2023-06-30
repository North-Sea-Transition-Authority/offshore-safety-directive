package uk.co.nstauthority.offshoresafetydirective;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.AutoConfigureJooq;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.nstauthority.offshoresafetydirective.configuration.EnergyPortalMessageQueueTestConfiguration;

/**
 * Test utility annotation which can be used to run a spring boot based test
 * with an in memory autoconfigured database
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "development", "application-integration-test"})
@Import(EnergyPortalMessageQueueTestConfiguration.class)
@AutoConfigureJooq
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
public @interface ApplicationIntegrationTest {
}