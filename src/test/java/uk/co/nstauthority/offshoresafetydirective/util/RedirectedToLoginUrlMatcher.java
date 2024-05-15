package uk.co.nstauthority.offshoresafetydirective.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.web.servlet.ResultMatcher;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class RedirectedToLoginUrlMatcher {

  private static final String SAML_LOGIN_REDIRECT_URL = "http://localhost/saml2/authenticate/saml";

  private RedirectedToLoginUrlMatcher() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static ResultMatcher redirectionToLoginUrl() {
    return result -> assertThat(result.getResponse().getRedirectedUrl())
        .isEqualTo(SAML_LOGIN_REDIRECT_URL);
  }

}
