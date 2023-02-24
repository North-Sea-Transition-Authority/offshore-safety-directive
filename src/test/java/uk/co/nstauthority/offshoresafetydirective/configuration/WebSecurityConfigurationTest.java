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
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.offshoresafetydirective.IntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.SystemOfRecordLandingPageController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.SystemOfRecordSearchController;

@AutoConfigureMockMvc
@TestPropertySource(properties = "energy-portal.logout-url=https://portal-logout-url.co.uk")
@IntegrationTest
class WebSecurityConfigurationTest {

  @Autowired
  private MockMvc mockMvc;

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

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).testEndpoint()))
            .with(user(user, emptyGrantedAuthorityCollection))
        )
        .andExpect(status().isForbidden());
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

  @ParameterizedTest
  @MethodSource("getUnauthenticatedEndpoints")
  void security_verifyUnauthenticatedEndpoints(String inputUrl) throws Exception {
    mockMvc.perform(get(inputUrl))
        .andExpect(status().isOk());
  }

  private static Stream<Arguments> getUnauthenticatedEndpoints() {
    return Stream.of(
        Arguments.of(ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage())),
        Arguments.of(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch())),
        Arguments.of(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch())),
        Arguments.of(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch())),
        Arguments.of(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderForwardAreaApprovalSearch()))
    );
  }
}