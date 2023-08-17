package uk.co.nstauthority.offshoresafetydirective.authorisation;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.interceptorutil.AppointmentInterceptorUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationService;

@Component
public class HasNotBeenTerminatedInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasNotBeenTerminated.class
  );

  private final AppointmentTerminationService appointmentTerminationService;

  @Autowired
  public HasNotBeenTerminatedInterceptor(AppointmentTerminationService appointmentTerminationService) {
    this.appointmentTerminationService = appointmentTerminationService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {
    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {
      var appointmentId = AppointmentInterceptorUtil.extractAppointmentIdFromRequest(request, handlerMethod);
      var hasBeenTerminated = appointmentTerminationService.hasBeenTerminated(appointmentId);

      var errorMessage = "No termination found with ID %s"
          .formatted(appointmentId.id());

      if (hasBeenTerminated) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
      }
    }
    return true;
  }
}
