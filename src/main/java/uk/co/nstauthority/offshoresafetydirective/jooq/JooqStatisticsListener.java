package uk.co.nstauthority.offshoresafetydirective.jooq;

import java.io.Serial;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.springframework.stereotype.Component;

@Component
public class JooqStatisticsListener implements ExecuteListener {

  @Serial
  private static final long serialVersionUID = 7399239846062763212L;

  public final transient ThreadLocal<Integer> count = ThreadLocal.withInitial(() -> 0);

  public Integer getCount() {
    return count.get();
  }

  public void clear() {
    count.remove();
  }

  @Override
  public void start(ExecuteContext ctx) {
    count.set(count.get() + 1);
  }
}
