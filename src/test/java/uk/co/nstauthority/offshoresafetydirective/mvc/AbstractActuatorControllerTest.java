package uk.co.nstauthority.offshoresafetydirective.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;

@DatabaseIntegrationTest
@AutoConfigureMockMvc
public abstract class AbstractActuatorControllerTest {

  @Autowired
  protected MockMvc mockMvc;
}
