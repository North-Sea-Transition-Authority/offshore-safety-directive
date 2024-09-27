package uk.co.nstauthority.offshoresafetydirective.mvc;

import com.google.common.base.Stopwatch;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.nstauthority.offshoresafetydirective.authentication.EnergyPortalSamlAttribute;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.jooq.JooqStatisticsListener;
import uk.co.nstauthority.offshoresafetydirective.jpa.HibernateQueryCounterImpl;

@Component
public class RequestLogFilter extends OncePerRequestFilter {

  static final String MDC_WUA_ID = RequestLogFilter.class.getName() + ".%s".formatted(
      EnergyPortalSamlAttribute.WEB_USER_ACCOUNT_ID.getAttributeName()
  );
  static final String MDC_PROXY_WUA_ID = RequestLogFilter.class.getName() + ".%s".formatted(
      EnergyPortalSamlAttribute.PROXY_USER_WUA_ID.getAttributeName()
  );
  static final String MDC_REQUEST_TYPE = RequestLogFilter.class.getName() + ".REQUEST_TYPE";
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogFilter.class);
  private static final String UNKNOWN = "unknown";

  private final HibernateQueryCounterImpl hibernateQueryCounter;
  private final JooqStatisticsListener jooqStatisticsListener;

  @Autowired
  public RequestLogFilter(HibernateQueryCounterImpl hibernateQueryCounter,
                          JooqStatisticsListener jooqStatisticsListener) {
    this.hibernateQueryCounter = hibernateQueryCounter;
    this.jooqStatisticsListener = jooqStatisticsListener;
  }

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request,
                                  @NotNull HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    var stopwatch = Stopwatch.createStarted();
    hibernateQueryCounter.clearQueryCount();
    String correlationId = "";

    try {
      correlationId = CorrelationIdUtil.getOrCreateCorrelationId(request);
      CorrelationIdUtil.setCorrelationIdOnMdc(correlationId);
      filterChain.doFilter(request, response);
    } finally {
      String queryString = StringUtils.defaultString(request.getQueryString());
      if (queryString.length() > 0) {
        queryString = "?" + queryString;
      }

      Long hibernateQueryCount = hibernateQueryCounter.getQueryCount();

      Object patternAttribute = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
      String mvcPattern = StringUtils.firstNonBlank(patternAttribute != null ? patternAttribute.toString() : "",
          UNKNOWN);

      String requestType = StringUtils.firstNonBlank(MDC.get(MDC_REQUEST_TYPE), UNKNOWN);
      String userId = StringUtils.firstNonBlank(MDC.get(MDC_WUA_ID), UNKNOWN);
      String proxyUserId = StringUtils.firstNonBlank(MDC.get(MDC_PROXY_WUA_ID), UNKNOWN);

      var jooqQueryCount = jooqStatisticsListener.getCount();
      jooqStatisticsListener.clear();
      CorrelationIdUtil.clearCorrelationIdOnMdc();
      MDC.remove(RequestLogFilter.MDC_WUA_ID);
      MDC.remove(RequestLogFilter.MDC_PROXY_WUA_ID);

      LOGGER.info(
          "{} request: {} {}{} ({}), correlation id: {}, time: {}, status: {}, user id: {}, proxy user id: {}, " +
              "hibernate query count: {}, jooq query count: {}",
          requestType, request.getMethod(), request.getRequestURI(), queryString,
          mvcPattern, correlationId, stopwatch.elapsed(TimeUnit.MILLISECONDS),
          response.getStatus(), userId, proxyUserId, hibernateQueryCount, jooqQueryCount);
    }
  }
}
