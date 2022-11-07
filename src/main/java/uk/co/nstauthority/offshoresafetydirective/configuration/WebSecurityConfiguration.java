package uk.co.nstauthority.offshoresafetydirective.configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.web.SecurityFilterChain;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlResponseParser;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceLogoutSuccessHandler;

@Configuration
public class WebSecurityConfiguration {

  static final String IDP_ACCESS_GRANTED_AUTHORITY_NAME = "WIOS_ACCESS_PRIVILEGE";

  private final SamlProperties samlProperties;
  private final SamlResponseParser samlResponseParser;
  private final ServiceLogoutSuccessHandler serviceLogoutSuccessHandler;


  @Autowired
  public WebSecurityConfiguration(SamlProperties samlProperties,
                                  SamlResponseParser samlResponseParser,
                                  ServiceLogoutSuccessHandler serviceLogoutSuccessHandler) {
    this.samlProperties = samlProperties;
    this.samlResponseParser = samlResponseParser;
    this.serviceLogoutSuccessHandler = serviceLogoutSuccessHandler;
  }

  @Bean
  protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

    var authenticationProvider = new OpenSaml4AuthenticationProvider();
    authenticationProvider.setResponseAuthenticationConverter(r -> samlResponseParser.parseSamlResponse(r.getResponse()));

    httpSecurity
        .authorizeHttpRequests()
        .mvcMatchers("/*")
          .hasAuthority(IDP_ACCESS_GRANTED_AUTHORITY_NAME)
        .mvcMatchers("/assets/**")
          .permitAll()
        .anyRequest()
          .authenticated()
        .and()
        .saml2Login(saml2 -> saml2.authenticationManager(new ProviderManager(authenticationProvider)))
        .logout()
          .logoutSuccessHandler(serviceLogoutSuccessHandler);

    return httpSecurity.build();
  }

  @Bean
  protected RelyingPartyRegistrationRepository relyingPartyRegistrations() throws Exception {
    RelyingPartyRegistration registration = getRelyingPartyRegistration();
    return new InMemoryRelyingPartyRegistrationRepository(registration);
  }

  @Bean
  public RelyingPartyRegistration getRelyingPartyRegistration() throws CertificateException {

    var certificateStream = new ByteArrayInputStream(samlProperties.getCertificate().getBytes(StandardCharsets.UTF_8));

    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
        .generateCertificate(certificateStream);

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
        .assertionConsumerServiceLocation(samlProperties.getConsumerServiceLocation())
        .build();
  }
}

