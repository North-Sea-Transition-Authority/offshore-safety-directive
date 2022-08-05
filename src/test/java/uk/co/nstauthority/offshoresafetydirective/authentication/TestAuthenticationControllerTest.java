package uk.co.nstauthority.offshoresafetydirective.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = {
    TestAuthenticationController.class
})
class TestAuthenticationControllerTest extends AbstractControllerTest {

  @Test
  public void authenticationRequired() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(TestAuthenticationController.class).renderSecured())))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  public void authorisedRequest() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(TestAuthenticationController.class).renderSecured())))
        .andExpect(status().isOk());
  }

}