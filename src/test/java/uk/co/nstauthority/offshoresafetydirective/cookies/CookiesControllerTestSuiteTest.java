package uk.co.nstauthority.offshoresafetydirective.cookies;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

class CookiesControllerTestSuiteTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Nested
  @ContextConfiguration(classes = CookiesController.class)
  @TestPropertySource(properties = {
      "analytics.serviceAnalyticIdentifier=G-service-analytic-identifier",
      "analytics.energyPortalAnalyticIdentifier=G-portal-analytic-identifier"
  })
  class WithGooglePrefix extends AbstractControllerTest {

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
          .andExpect(model().attribute("pageName", CookiesController.PAGE_NAME))
          .andExpect(model().attribute("serviceAnalyticIdentifier", "service-analytic-identifier"))
          .andExpect(model().attribute("energyPortalAnalyticIdentifier", "portal-analytic-identifier"));
    }
  }

  @Nested
  @ContextConfiguration(classes = CookiesController.class)
  @TestPropertySource(properties = {
      "analytics.serviceAnalyticIdentifier=not-G-service-analytic-identifier",
      "analytics.energyPortalAnalyticIdentifier=not-G-portal-analytic-identifier"
  })
  class WithoutGooglePrefix extends AbstractControllerTest {

    @Test
    void getCookiePreferences_whenNoGooglePrefix_assertModelProperties() throws Exception {
      mockMvc.perform(get(
              ReverseRouter.route(on(CookiesController.class).getCookiePreferences()))
              .with(user(USER)))
          .andExpect(status().isOk())
          .andExpect(view().name("osd/cookies/cookies"))
          .andExpect(model().attribute("pageName", CookiesController.PAGE_NAME))
          .andExpect(model().attribute("serviceAnalyticIdentifier", "not-G-service-analytic-identifier"))
          .andExpect(model().attribute("energyPortalAnalyticIdentifier", "not-G-portal-analytic-identifier"));
    }
  }
}