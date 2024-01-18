package uk.co.nstauthority.offshoresafetydirective.cookies;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = CookiesController.class)
class CookiesControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @SecurityTest
  void getCookiePreferences_withNoUser_assertStatusOk() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CookiesController.class).getCookiePreferences())))
        .andExpect(view().name("osd/cookies/cookies"))
        .andExpect(status().isOk());
  }

  @Test
  void getCookiePreferences_assertModelProperties() throws Exception {
    mockMvc.perform(get(
            ReverseRouter.route(on(CookiesController.class).getCookiePreferences()))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/cookies/cookies"))
        .andExpect(model().attribute("pageName", CookiesController.PAGE_NAME));
  }
}