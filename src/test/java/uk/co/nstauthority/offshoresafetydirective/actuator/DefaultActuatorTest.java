package uk.co.nstauthority.offshoresafetydirective.actuator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractActuatorControllerTest;

class DefaultActuatorTest extends AbstractActuatorControllerTest {

  @Test
  void health() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk());
  }
}
