package uk.co.nstauthority.offshoresafetydirective.mvc;

import static net.logstash.logback.argument.StructuredArguments.value;

import com.google.common.base.Stopwatch;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.nstauthority.offshoresafetydirective.authentication.EnergyPortalSamlAttribute;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.metrics.QueryCounter;

@Component
public class RequestLogFilter extends OncePerRequestFilter {

  static final String MDC_WUA_ID = RequestLogFilter.class.getName() + ".%s".formatted(
      EnergyPortalSamlAttribute.WEB_USER_ACCOUNT_ID.getAttributeName()
  );
  static final String MDC_PROXY_WUA_ID = RequestLogFilter.class.getName() + ".%s".formatted(
      EnergyPortalSamlAttribute.PROXY_USER_WUA_ID.getAttributeName()
  );
  static final String MDC_REQUEST_TYPE = RequestLogFilter.class.getName() + ".REQUEST_TYPE";
  private static final String UNKNOWN = "unknown";

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogFilter.class);

  private final QueryCounter queryCounter;

  RequestLogFilter(QueryCounter queryCounter) {
    this.queryCounter = queryCounter;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    var requestUri = request.getRequestURI();
    var contextPath = request.getContextPath();
    return requestUri.equals(contextPath + "/actuator/health")
        || requestUri.startsWith(contextPath + "/assets");
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    CorrelationIdUtil.setCorrelationIdOnMdcFromRequest(request);
    var stopwatch = Stopwatch.createStarted();

    try {
      filterChain.doFilter(request, response);
    } finally {
      var elapsedMs = stopwatch.elapsed(TimeUnit.MILLISECONDS);
      var queryString = Optional.ofNullable(request.getQueryString()).map("?"::concat).orElse("");
      var pattern = Optional.ofNullable(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).orElse(UNKNOWN);
      var wuaId = MDC.get(MDC_WUA_ID);
      var proxyWuaId = MDC.get(MDC_PROXY_WUA_ID);

      LOGGER.info(
          "[{}] {}ms {} {}{} ({}) wuaId:{} proxyWuaId:{} queryCounts[hibernate:{} epa:{}]",
          value("response_status", response.getStatus()),
          value("request_time_ms", elapsedMs),
          value("request_method", request.getMethod()),
          value("request_uri", request.getRequestURI()),
          value("request_query_string", queryString),
          value("request_uri_pattern", pattern),
          value("wua_id", wuaId),
          value("proxy_wua_id", proxyWuaId),
          value("query_count_hibernate", queryCounter.getAndResetHibernate()),
          value("query_count_epa", queryCounter.getAndResetEpa())
      );

      // remove MDC items set for use by the RequestLogFilter
      CorrelationIdUtil.removeCorrelationIdFromMdc();
      MDC.remove(RequestLogFilter.MDC_WUA_ID);
      MDC.remove(RequestLogFilter.MDC_PROXY_WUA_ID);
    }
  }
}
