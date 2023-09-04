package uk.co.nstauthority.offshoresafetydirective.interceptorutil;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.logging.LoggerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;

public class AppointmentInterceptorUtil {

  private AppointmentInterceptorUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static AppointmentId extractAppointmentIdFromRequest(HttpServletRequest httpServletRequest,
                                                              HandlerMethod handlerMethod) {

    var appointmentIdParameter = AbstractHandlerInterceptor.getPathVariableByClass(handlerMethod, AppointmentId.class);

    if (appointmentIdParameter.isEmpty()) {
      var errorMessage = "No path variable of type AppointmentId found in request";
      LoggerUtil.warn(errorMessage);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @SuppressWarnings("unchecked")
    var pathVariables = (Map<String, String>) httpServletRequest
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    return AppointmentId.valueOf(pathVariables.get(appointmentIdParameter.get().getName()));
  }
}
