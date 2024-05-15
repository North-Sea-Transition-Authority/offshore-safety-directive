package uk.co.nstauthority.offshoresafetydirective.authentication;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import uk.co.nstauthority.offshoresafetydirective.configuration.WebSecurityConfiguration;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class TestUserProvider {

  private static final GrantedAuthority IDP_GRANTED_AUTHORITY = new SimpleGrantedAuthority(
      WebSecurityConfiguration.IDP_ACCESS_GRANTED_AUTHORITY_NAME
  );

  private TestUserProvider() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static RequestPostProcessor user(ServiceUserDetail serviceUserDetail) {
    return user(serviceUserDetail, List.of(IDP_GRANTED_AUTHORITY));
  }

  public static RequestPostProcessor user(ServiceUserDetail serviceUserDetail,
                                          Collection<GrantedAuthority> authorities) {

    var authentication = SamlAuthenticationUtil.Builder()
        .withUser(serviceUserDetail)
        .withGrantedAuthorities(authorities)
        .build();

    return authentication(authentication);
  }
}