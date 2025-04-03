package uk.co.nstauthority.offshoresafetydirective.jpa;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.metrics.QueryCounter;

@Component
public class HibernateQueryCounter implements StatementInspector {

  // This is marked as transient because we don't want to serialise it. `StatementInspector` is serializable
  private final transient QueryCounter queryCounter;

  public HibernateQueryCounter(QueryCounter queryCounter) {
    this.queryCounter = queryCounter;
  }

  @Override
  public String inspect(String sql) {
    queryCounter.incrementHibernate();
    return sql;
  }

}
