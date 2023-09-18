package uk.co.nstauthority.offshoresafetydirective.configuration;

import javax.sql.DataSource;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import uk.co.nstauthority.offshoresafetydirective.jooq.JooqStatisticsListener;

@Configuration
class JooqConfiguration {

  private final DataSource dataSource;
  private final JooqStatisticsListener jooqStatisticsListener;

  @Autowired
  JooqConfiguration(DataSource dataSource, JooqStatisticsListener jooqStatisticsListener) {
    this.dataSource = dataSource;
    this.jooqStatisticsListener = jooqStatisticsListener;
  }

  @Bean
  public DataSourceConnectionProvider connectionProvider() {
    return new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(dataSource));
  }

  @Bean
  public DefaultDSLContext dsl(DefaultConfiguration configuration) {
    return new DefaultDSLContext(configuration);
  }

  @Bean
  public DefaultConfiguration configuration(DataSourceConnectionProvider connectionProvider) {
    var jooqConfiguration = new DefaultConfiguration();
    jooqConfiguration.setSQLDialect(SQLDialect.POSTGRES);
    jooqConfiguration.set(connectionProvider);
    jooqConfiguration.set(new DefaultExecuteListenerProvider(jooqStatisticsListener));

    var settings  = new Settings()
        .withRenderQuotedNames(RenderQuotedNames.NEVER)
        .withInterpreterDialect(SQLDialect.POSTGRES)
        .withParseDialect(SQLDialect.POSTGRES)
        .withRenderSchema(true);

    jooqConfiguration.setSettings(settings);

    return jooqConfiguration;
  }

  @Bean
  public Schema getDefaultSchema(JooqConfigurationProperties jooqConfigurationProperties) {
    return DSL.schema(jooqConfigurationProperties.schema());
  }

}