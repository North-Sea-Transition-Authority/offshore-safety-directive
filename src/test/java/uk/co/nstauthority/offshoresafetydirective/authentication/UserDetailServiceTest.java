package uk.co.nstauthority.offshoresafetydirective.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

class UserDetailServiceTest {

  private static UserDetailService userDetailService;

  @BeforeAll
  static void beforeAll() {
    userDetailService = new UserDetailService();
  }

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
  }

  @Test
  void getUserDetail_whenUserInContext_thenExpectedUserReturned() {

    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(100L)
        .build();

    SamlAuthenticationUtil.Builder()
        .withUser(user)
        .setSecurityContext();

    assertThat(userDetailService.getUserDetail())
        .extracting(ServiceUserDetail::wuaId)
        .isEqualTo(user.wuaId());
  }

  @Test
  void getUserDetail_whenNoPrincipal_thenException() {

    SecurityContextHolder.setContext(new SecurityContextImpl(new ServiceSaml2Authentication(null, Set.of())));

    assertThrowsExactly(InvalidAuthenticationException.class, () -> userDetailService.getUserDetail(),
        "ServiceUserDetails not found in ServiceSaml2Authentication principal");
  }

  @Test
  void getUserDetail_whenNoAuthenticationInContext_thenException() {
    assertThrowsExactly(InvalidAuthenticationException.class, () -> userDetailService.getUserDetail(),
        "ServiceSaml2Authentication not found in authentication context");
  }

  @Test
  void isUserLoggedIn_whenUserInContext_thenExpectTrue() {

    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(100L)
        .build();

    SamlAuthenticationUtil.Builder()
        .withUser(user)
        .setSecurityContext();

    assertTrue(userDetailService.isUserLoggedIn());
  }

  @Test
  void isUserLoggedIn_whenNoPrincipal_thenException() {
    assertFalse(userDetailService.isUserLoggedIn());
  }

  @Test
  void isUserLoggedIn_whenNoAuthenticationInContext_thenException() {
    SecurityContextHolder.setContext(new SecurityContextImpl(new ServiceSaml2Authentication(null, Set.of())));
    assertFalse(userDetailService.isUserLoggedIn());
  }

  @Test
  void getOptionalUserDetail_whenUserLoggedIn_thenPopulatedOptional() {

    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(100L)
        .build();

    SamlAuthenticationUtil.Builder()
        .withUser(user)
        .setSecurityContext();

    var resultingUser = userDetailService.getOptionalUserDetail();

    assertThat(resultingUser).contains(user);
  }

  @Test
  void getOptionalUserDetail_whenPrincipleNotFoundInServiceSaml2Authentication_thenEmptyOptional() {
    SecurityContextHolder.setContext(new SecurityContextImpl(new ServiceSaml2Authentication(null, Set.of())));
    var resultingUser = userDetailService.getOptionalUserDetail();
    assertThat(resultingUser).isEmpty();
  }

  @Test
  void getOptionalUserDetail_whenServiceSaml2AuthenticationNotFoundInAuthenticationContext_thenEmptyOptional() {
    SecurityContextHolder.setContext(new SecurityContextImpl());
    var resultingUser = userDetailService.getOptionalUserDetail();
    assertThat(resultingUser).isEmpty();
  }
}