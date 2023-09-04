package uk.co.nstauthority.offshoresafetydirective.nomination;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.interceptorutil.NominationInterceptorUtil;
import uk.co.nstauthority.offshoresafetydirective.logging.LoggerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;

@Component
public class NominationInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasNominationStatus.class
  );

  private final NominationDetailService nominationDetailService;

  @Autowired
  NominationInterceptor(NominationDetailService nominationDetailService) {
    this.nominationDetailService = nominationDetailService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {
      var nominationId = NominationInterceptorUtil.extractNominationIdFromRequest(request, handlerMethod);

      NominationDetail nominationDetail;
      var annotation = (HasNominationStatus) getAnnotation(handlerMethod, HasNominationStatus.class);

      nominationDetail = switch (annotation.fetchType()) {
        case LATEST -> nominationDetailService.getLatestNominationDetail(nominationId);
        case LATEST_POST_SUBMISSION -> nominationDetailService.getLatestNominationDetailWithStatuses(
            nominationId,
            NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
        ).orElse(null);
      };

      if (nominationDetail == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "A non null nomination detail was expected to be found for nomination id %s".formatted(nominationId.id())
        );
      }

      if (hasAnnotation(handlerMethod, HasNominationStatus.class)) {
        checkNominationStatus(handlerMethod, nominationDetail);
      }
    }

    return true;
  }

  private void checkNominationStatus(HandlerMethod handlerMethod, NominationDetail nominationDetail) {

    var allowedStatuses = Arrays.asList(
        ((HasNominationStatus) getAnnotation(handlerMethod, HasNominationStatus.class)).statuses()
    );

    if (!allowedStatuses.contains(nominationDetail.getStatus())) {

      var allowedStatusNames = allowedStatuses
          .stream()
          .map(NominationStatus::name)
          .toList();

      var errorMessage = "Nomination detail with ID %s has status %s but requires %s"
          .formatted(
              nominationDetail.getId(),
              nominationDetail.getStatus().name(),
              StringUtils.join(allowedStatusNames)
          );

      LoggerUtil.warn(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
    }
  }

}
