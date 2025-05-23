package uk.co.nstauthority.offshoresafetydirective.configuration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.mvc.error.DefaultClientErrorController;

@AutoConfigureMockMvc
@DatabaseIntegrationTest
class WebSecurityConfigurationTest {

  private static final String CONTEXT_PATH = "/test";

  @Autowired
  private MockMvc mockMvc;

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("server.servlet.context-path", () -> CONTEXT_PATH);
    registry.add("energy-portal.logout-url", () -> "https://portal-logout-url.co.uk");
  }

  @Test
  void security_whenUserHasAccessAuthority_thenOk() throws Exception {

    var idpAccessGrantedAuthority = new SimpleGrantedAuthority(WebSecurityConfiguration.IDP_ACCESS_GRANTED_AUTHORITY_NAME);

    var user = ServiceUserDetailTestUtil.Builder().build();

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).testEndpoint()))
            .with(user(user, Set.of(idpAccessGrantedAuthority)))
        )
        .andExpect(status().isOk());
  }

  @Test
  void security_whenUserDoesntHaveAccessAuthority_thenForbidden() throws Exception {

    Set<GrantedAuthority> emptyGrantedAuthorityCollection = Set.of();

    var user = ServiceUserDetailTestUtil.Builder().build();

    // check for a single path endpoint, e.g. /endpoint
    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).testEndpoint()))
            .with(user(user, emptyGrantedAuthorityCollection))
      )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            CONTEXT_PATH + ReverseRouter.route(on(DefaultClientErrorController.class).getUnauthorisedErrorPage())
        ));

    // check for a multi path endpoint e.g. /first/second
    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).testEndpointWithMultiUrlPath()))
            .with(user(user, emptyGrantedAuthorityCollection))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            CONTEXT_PATH + ReverseRouter.route(on(DefaultClientErrorController.class).getUnauthorisedErrorPage())
        ));
  }

  @Test
  void security_whenLogout_thenEnergyPortalLogoutRedirectionFollowed() throws Exception {

    var idpAccessGrantedAuthority = new SimpleGrantedAuthority(WebSecurityConfiguration.IDP_ACCESS_GRANTED_AUTHORITY_NAME);

    var user = ServiceUserDetailTestUtil.Builder().build();

    mockMvc.perform(post("/logout")
            .with(user(user, Set.of(idpAccessGrantedAuthority)))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("https://portal-logout-url.co.uk"))
        .andExpect(unauthenticated());
  }
}