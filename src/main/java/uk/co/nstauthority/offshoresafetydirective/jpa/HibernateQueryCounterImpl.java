package uk.co.nstauthority.offshoresafetydirective.jpa;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

@Component
public class HibernateQueryCounterImpl implements StatementInspector {

  private final transient ThreadLocal<Long> threadQueryCount = ThreadLocal.withInitial(() -> 0L);

  public Long getQueryCount() {
    return threadQueryCount.get();
  }

  public void clearQueryCount() {
    threadQueryCount.remove();
  }

  @Override
  public String inspect(String sql) {
    threadQueryCount.set(threadQueryCount.get() + 1);
    return sql;
  }
}
