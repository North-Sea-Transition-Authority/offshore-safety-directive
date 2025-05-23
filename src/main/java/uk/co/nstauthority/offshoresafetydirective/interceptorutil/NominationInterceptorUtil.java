package uk.co.nstauthority.offshoresafetydirective.interceptorutil;

import static uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor.getPathVariableByClass;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

public class NominationInterceptorUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(NominationInterceptorUtil.class);

  private NominationInterceptorUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static NominationId extractNominationIdFromRequest(HttpServletRequest httpServletRequest,
                                                            HandlerMethod handlerMethod) {

    var nominationIdParameter = getPathVariableByClass(handlerMethod, NominationId.class);

    if (nominationIdParameter.isEmpty()) {
      var errorMessage = "No path variable of type NominationId found in request";
      LOGGER.warn(errorMessage);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @SuppressWarnings("unchecked")
    var pathVariables = (Map<String, String>) httpServletRequest
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    return NominationId.valueOf(pathVariables.get(nominationIdParameter.get().getName()));
  }
}
