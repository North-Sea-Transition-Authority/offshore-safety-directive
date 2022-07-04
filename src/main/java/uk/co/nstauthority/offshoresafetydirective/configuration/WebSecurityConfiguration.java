package uk.co.nstauthority.offshoresafetydirective.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final SamlProperties samlProperties;

  @Autowired
  public WebSecurityConfiguration(SamlProperties samlProperties) {
    this.samlProperties = samlProperties;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests()
        .mvcMatchers("/")
          .permitAll()
        .anyRequest()
          .authenticated()
        .and()
        .saml2Login();
  }

  @Bean
  protected RelyingPartyRegistrationRepository relyingPartyRegistrations() throws Exception {
    RelyingPartyRegistration registration = getRelyingPartyRegistration();
    return new InMemoryRelyingPartyRegistrationRepository(registration);
  }

  @Bean
  public RelyingPartyRegistration getRelyingPartyRegistration() throws IOException, CertificateException {
    Resource resource = new ClassPathResource(samlProperties.getCertificateLocation());
    InputStream inputStream = resource.getInputStream();
    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
    Saml2X509Credential credential = Saml2X509Credential.verification(Objects.requireNonNull(certificate));
    return RelyingPartyRegistration
        .withRegistrationId(samlProperties.getRegistrationId())
        .assertingPartyDetails(party -> party
            .entityId(samlProperties.getEntityId())
            .singleSignOnServiceLocation(samlProperties.getLoginUrl())
            .singleSignOnServiceBinding(Saml2MessageBinding.POST)
            .wantAuthnRequestsSigned(false)
            .verificationX509Credentials(c -> c.add(credential))
        )
        .build();
  }

}

