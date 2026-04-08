package uk.co.nstauthority.offshoresafetydirective;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.boot.test.autoconfigure.jooq.AutoConfigureJooq;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.co.nstauthority.offshoresafetydirective.configuration.EnergyPortalMessageQueueTestConfiguration;

@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"development", "database-integration-test"})
@Import(EnergyPortalMessageQueueTestConfiguration.class)
@AutoConfigureJooq
@DirtiesContext(classMode = AFTER_CLASS)
public @interface DatabaseIntegrationTest {
}
