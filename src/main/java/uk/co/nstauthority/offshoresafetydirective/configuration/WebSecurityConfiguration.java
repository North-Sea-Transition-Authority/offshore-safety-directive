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
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlResponseParser;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceLogoutSuccessHandler;
import uk.co.nstauthority.offshoresafetydirective.authorisation.ServiceAccessDeniedHandler;
import uk.co.nstauthority.offshoresafetydirective.mvc.PostAuthenticationRequestMdcFilter;
import uk.co.nstauthority.offshoresafetydirective.mvc.RequestLogFilter;

@Configuration
public class WebSecurityConfiguration {

  public static final String IDP_ACCESS_GRANTED_AUTHORITY_NAME = "WIOS_ACCESS_PRIVILEGE";

  private static final String[] NO_AUTH_ENDPOINTS = {
      "/assets/**",
      "/system-of-record/**",
      "/api/public/**",
      "/accessibility-statement",
      "/error/**",
      "/api/v1/logout/**",
      "/contact",
      "/cookies"
  };

  private final SamlProperties samlProperties;
  private final SamlResponseParser samlResponseParser;
  private final ServiceLogoutSuccessHandler serviceLogoutSuccessHandler;
  private final RequestLogFilter requestLogFilter;
  private final PostAuthenticationRequestMdcFilter postAuthenticationRequestMdcFilter;


  @Autowired
  public WebSecurityConfiguration(SamlProperties samlProperties,
                                  SamlResponseParser samlResponseParser,
                                  ServiceLogoutSuccessHandler serviceLogoutSuccessHandler, RequestLogFilter requestLogFilter,
                                  PostAuthenticationRequestMdcFilter postAuthenticationRequestMdcFilter) {
    this.samlProperties = samlProperties;
    this.samlResponseParser = samlResponseParser;
    this.serviceLogoutSuccessHandler = serviceLogoutSuccessHandler;
    this.requestLogFilter = requestLogFilter;
    this.postAuthenticationRequestMdcFilter = postAuthenticationRequestMdcFilter;
  }

  @Bean
  @Order(2)
  protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

    var authenticationProvider = new OpenSaml4AuthenticationProvider();
    authenticationProvider.setResponseAuthenticationConverter(
        responseToken -> samlResponseParser.parseSamlResponse(responseToken.getResponse())
    );

    httpSecurity
        .authorizeHttpRequests(request -> request
            .requestMatchers(NO_AUTH_ENDPOINTS).permitAll()
            .anyRequest().hasAuthority(IDP_ACCESS_GRANTED_AUTHORITY_NAME)
        )
        .csrf(new CsrfCustomizer(NO_AUTH_ENDPOINTS))
        .saml2Login(saml2 -> saml2.authenticationManager(new ProviderManager(authenticationProvider)))
        .logout(logout -> logout.logoutSuccessHandler(serviceLogoutSuccessHandler))
        .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedHandler(accessDeniedHandler()));

    httpSecurity.addFilterBefore(requestLogFilter, SecurityContextHolderFilter.class);
    httpSecurity.addFilterAfter(postAuthenticationRequestMdcFilter, SecurityContextHolderFilter.class);
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

  @Bean
  AccessDeniedHandler accessDeniedHandler() {
    return new ServiceAccessDeniedHandler();
  }
}

