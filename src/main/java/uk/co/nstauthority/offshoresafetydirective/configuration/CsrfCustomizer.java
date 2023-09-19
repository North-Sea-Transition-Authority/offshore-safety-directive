package uk.co.nstauthority.offshoresafetydirective.configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;

class CsrfCustomizer implements Customizer<CsrfConfigurer<HttpSecurity>> {

  private final String[] ignoredUrls;

  CsrfCustomizer(String... ignoredUrls) {
    this.ignoredUrls = ignoredUrls;
  }

  @Override
  public void customize(CsrfConfigurer<HttpSecurity> httpSecurityCsrfConfigurer) {
    httpSecurityCsrfConfigurer
        .ignoringRequestMatchers(ignoredUrls);
  }

}
