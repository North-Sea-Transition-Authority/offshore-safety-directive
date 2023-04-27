package uk.co.nstauthority.offshoresafetydirective.correlationid;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

class CorrelationIdHttpRequestFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationIdHttpRequestFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    try {
      String correlationId = CorrelationIdUtil.getOrCreateCorrelationId(request);
      CorrelationIdUtil.setCorrelationIdOnMdc(correlationId);
      LOGGER.debug("Assigned MDC correlation ID {} [request URI '{}']", correlationId, request.getRequestURI());

      filterChain.doFilter(request, response);
    } finally {
      CorrelationIdUtil.clearCorrelationIdOnMdc();
    }
  }
}
