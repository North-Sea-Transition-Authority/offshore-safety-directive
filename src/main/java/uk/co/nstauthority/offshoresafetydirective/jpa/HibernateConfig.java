package uk.co.nstauthority.offshoresafetydirective.jpa;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

  @Bean
  public HibernatePropertiesCustomizer configureStatementInspector(HibernateQueryCounterImpl hibernateQueryCounter) {
    return properties -> properties.put(AvailableSettings.STATEMENT_INSPECTOR, hibernateQueryCounter);
  }
}