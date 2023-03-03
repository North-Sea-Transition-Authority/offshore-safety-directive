package uk.co.nstauthority.offshoresafetydirective.correlationid;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CorrelationIdFilterConfiguration {

  @Bean
  FilterRegistrationBean<CorrelationIdHttpRequestFilter> setCorrelationIdOnHttpRequestFilterRegistration() {
    var bean = new FilterRegistrationBean<CorrelationIdHttpRequestFilter>();
    bean.setFilter(new CorrelationIdHttpRequestFilter());
    bean.setOrder(1);
    return bean;
  }
}
