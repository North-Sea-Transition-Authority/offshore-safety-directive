package uk.co.nstauthority.offshoresafetydirective.configuration;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import uk.co.nstauthority.offshoresafetydirective.actuator.ActuatorConfigurationProperties;

@Configuration
class ActuatorWebSecurityConfiguration {

  @Bean
  @Order(1)
  SecurityFilterChain actuatorSecurityFilterChain(
      HttpSecurity httpSecurity,
      PasswordEncoder passwordEncoder,
      ActuatorConfigurationProperties actuatorConfigurationProperties
  ) throws Exception {
    var user = User.builder()
        .username("admin")
        .password(passwordEncoder.encode(actuatorConfigurationProperties.adminUserPassword()))
        .authorities("ACTUATOR")
        .build();
    var userDetailsService = new InMemoryUserDetailsManager(user);

    httpSecurity
        .requestMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeHttpRequests()
        .mvcMatchers("/actuator/health")
        .permitAll()
        .mvcMatchers("/*")
        .hasAuthority("ACTUATOR")
        .and()
        .httpBasic()
        .and()
        .userDetailsService(userDetailsService)
        .csrf()
        .disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    return httpSecurity.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
