package uk.co.nstauthority.offshoresafetydirective.correlationid;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

class CorrelationIdHttpRequestFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationIdHttpRequestFilter.class);

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
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
