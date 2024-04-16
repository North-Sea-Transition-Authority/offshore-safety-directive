package uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.interceptorutil.AppointmentInterceptorUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;

@Component
public class HasAppointmentStatusInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasAppointmentStatus.class
  );

  private final AppointmentAccessService appointmentAccessService;

  @Autowired
  public HasAppointmentStatusInterceptor(AppointmentAccessService appointmentAccessService) {
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

      var appointment = appointmentAccessService.getAppointment(appointmentId)
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.NOT_FOUND,
              "Appointment with ID [%s] was not found".formatted(
                  appointmentId
              )
          ));

      var allowedStatuses = ((HasAppointmentStatus) getAnnotation(handlerMethod, HasAppointmentStatus.class)).value();

      if (Arrays.stream(allowedStatuses).noneMatch(
          appointmentStatus -> appointmentStatus.equals(appointment.getAppointmentStatus())
      )) {

        var allowedStatusString = Arrays.stream(allowedStatuses)
            .map(Enum::name)
            .collect(Collectors.joining(","));

        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Appointment [%s] expected a status of [%s] but was [%s]".formatted(
                appointmentId,
                allowedStatusString,
                appointment.getAppointmentStatus()
            )
        );
      }

    }
    return true;
  }
}