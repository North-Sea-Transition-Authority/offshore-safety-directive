package uk.co.nstauthority.offshoresafetydirective.authenticationTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import uk.co.nstauthority.offshoresafetydirective.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@WebMvcTest
@ContextConfiguration(classes = {
    AuthenticationController.class
})
class AuthenticationControllerTest extends AbstractControllerTest {

  @Test
  public void authenticationRequired() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(AuthenticationController.class).renderSecured())))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  public void authorisedRequest() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(AuthenticationController.class).renderSecured())))
        .andExpect(status().isOk());
  }

}