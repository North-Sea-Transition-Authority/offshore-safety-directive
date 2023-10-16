package uk.co.nstauthority.offshoresafetydirective.configuration;

import static net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider.Configuration.builder;

import java.time.Clock;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class ApplicationConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }

  @Bean("messageSource")
  public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("messages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

  @Bean
  public LockProvider lockProvider(JdbcTemplate jdbcTemplate, DatabaseConfigurationProperties databaseConfigurationProperties) {
    return new JdbcTemplateLockProvider(builder()
        .withTableName("%s.shedlock".formatted(databaseConfigurationProperties.schema()))
        .withJdbcTemplate(jdbcTemplate)
        .usingDbTime()
        .build()
    );
  }
}
