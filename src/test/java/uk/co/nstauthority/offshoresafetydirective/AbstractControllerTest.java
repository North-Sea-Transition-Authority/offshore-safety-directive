package uk.co.nstauthority.offshoresafetydirective;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@WithDefaultPageControllerAdvice
public abstract class AbstractControllerTest {

  @Autowired
  protected MockMvc mockMvc;

}
