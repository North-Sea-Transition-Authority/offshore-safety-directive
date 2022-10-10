package uk.co.nstauthority.offshoresafetydirective.authentication;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class TestUserProvider {

  private TestUserProvider() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static RequestPostProcessor user(ServiceUserDetail serviceUserDetail) {
    return user(serviceUserDetail, Collections.emptySet());
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