package uk.co.nstauthority.offshoresafetydirective.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.interceptorutil.AppointmentInterceptorUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;

@Component
public class IsCurrentAppointmentInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      IsCurrentAppointment.class
  );
  private final AppointmentAccessService appointmentAccessService;

  @Autowired
  public IsCurrentAppointmentInterceptor(AppointmentAccessService appointmentAccessService) {
    this.appointmentAccessService = appointmentAccessService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {
    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {
      var appointmentId = AppointmentInterceptorUtil.extractAppointmentIdFromRequest(request, handlerMethod);

      Optional<AppointmentDto> appointmentOptional = appointmentAccessService.findAppointmentDtoById(appointmentId);

      if (appointmentOptional.isEmpty()) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No appointment found with ID %s".formatted(appointmentId.id()));
      }

      var appointment = appointmentOptional.get();

      if (appointment.appointmentToDate() != null && appointment.appointmentToDate().value() != null) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Cannot terminate appointment with ID %s as the appointment is not current".formatted(appointmentId.id())
        );
      }
    }

    return true;
  }
}