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
import uk.co.nstauthority.offshoresafetydirective.interceptorutil.AssetInterceptorUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;

@Component
public class HasAssetStatusInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasAssetStatus.class
  );
  private final AssetAccessService assetAccessService;
  private final AppointmentAccessService appointmentAccessService;

  @Autowired
  public HasAssetStatusInterceptor(AssetAccessService assetAccessService, AppointmentAccessService appointmentAccessService) {
    this.assetAccessService = assetAccessService;
    this.appointmentAccessService = appointmentAccessService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {
    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {
      var assetId = AssetInterceptorUtil.extractAssetIdFromRequest(request, handlerMethod);

      AssetStatus[] allowedStatuses = ((HasAssetStatus) getAnnotation(handlerMethod, HasAssetStatus.class)).value();
      AssetDto assetDto;

      if (assetId.isPresent()) {
        assetDto = assetAccessService.getAsset(assetId.get())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Asset with AssetId [%s] was not found".formatted(assetId)
            ));

      } else {
        var appointmentId = AppointmentInterceptorUtil.extractAppointmentIdFromRequest(request, handlerMethod);
        var appointment = appointmentAccessService.getAppointment(appointmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Appointment with ID [%s] was not found".formatted(
                    appointmentId
                )
            ));

        assetDto = AssetDto.fromAsset(appointment.getAsset());
      }

      checkAllowedStatuses(allowedStatuses, assetDto);
    }

    return true;
  }

  private void checkAllowedStatuses(AssetStatus[] allowedStatuses, AssetDto asset) {
    if (Arrays.stream(allowedStatuses).noneMatch(
        assetStatus -> assetStatus.equals(asset.status())
    )) {

      var allowedStatusString = Arrays.stream(allowedStatuses)
          .map(Enum::name)
          .collect(Collectors.joining(","));

      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Asset [%s] expected a status of [%s] but was [%s]".formatted(
              asset.assetId().id(),
              allowedStatusString,
              asset.status()
          )
      );
    }
  }
}
