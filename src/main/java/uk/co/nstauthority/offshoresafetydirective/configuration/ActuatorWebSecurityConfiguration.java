package uk.co.nstauthority.offshoresafetydirective.configuration;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class ActuatorWebSecurityConfiguration {

  @Bean
  @Order(1)
  SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .requestMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeHttpRequests()
        .mvcMatchers("/actuator/health")
        .permitAll()
        .mvcMatchers("/*")
        .denyAll();
    return httpSecurity.build();
  }
}
