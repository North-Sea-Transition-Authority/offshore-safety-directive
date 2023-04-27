package uk.co.nstauthority.offshoresafetydirective.configuration;

import java.util.Map;
import java.util.concurrent.Executor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
// Async method execution is not enabled for the integration-test profile, as some of our integration tests rely on
// async listeners firing before their assertions are called.
@Profile("!integration-test")
class AsyncConfiguration implements AsyncConfigurer {

  // https://moelholm.com/blog/2017/07/24/spring-43-using-a-taskdecorator-to-copy-mdc-data-to-async-threads
  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setTaskDecorator(new CorrelationIdTaskDecorator());
    executor.initialize();
    return executor;
  }

  private static class CorrelationIdTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
      Map<String, String> contextMap = MDC.getCopyOfContextMap();
      return () -> {
        try {
          MDC.setContextMap(contextMap);
          runnable.run();
        } finally {
          MDC.clear();
        }
      };
    }
  }
}
