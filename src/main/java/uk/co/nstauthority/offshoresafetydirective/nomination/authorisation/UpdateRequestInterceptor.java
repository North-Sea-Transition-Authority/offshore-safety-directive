package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.interceptorutil.NominationInterceptorUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;

@Service
public class UpdateRequestInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasUpdateRequest.class,
      HasNoUpdateRequest.class
  );

  private final NominationDetailService nominationDetailService;
  private final CaseEventQueryService caseEventQueryService;

  @Autowired
  UpdateRequestInterceptor(NominationDetailService nominationDetailService,
                           CaseEventQueryService caseEventQueryService) {
    this.nominationDetailService = nominationDetailService;
    this.caseEventQueryService = caseEventQueryService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {

      var requiresUpdateRequest = hasAnnotation(handlerMethod, HasUpdateRequest.class);
      var requiresNoUpdateRequest = hasAnnotation(handlerMethod, HasNoUpdateRequest.class);

      if (requiresUpdateRequest && requiresNoUpdateRequest) {
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Endpoint should only have one of [%s]".formatted(
                SUPPORTED_SECURITY_ANNOTATIONS.stream()
                    .map(Class::getName)
                    .collect(Collectors.joining(","))
            )
        );
      }

      var nominationId = NominationInterceptorUtil.extractNominationIdFromRequest(request, handlerMethod);

      Set<NominationStatus> allPostSubmissionStatuses = NominationStatus
          .getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);

      Optional<NominationDetail> nominationDetailOptional = nominationDetailService
          .getLatestNominationDetailWithStatuses(nominationId, allPostSubmissionStatuses);

      if (nominationDetailOptional.isEmpty()) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Could not find latest submitted NominationDetail for nomination with ID %s".formatted(nominationId.id()));
      }

      var nominationDetail = nominationDetailOptional.get();

      var hasUpdateRequest = caseEventQueryService.hasUpdateRequest(nominationDetail);

      if (requiresUpdateRequest && !hasUpdateRequest) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Update request was expected for nomination with ID [%s]".formatted(nominationId.id())
        );
      }

      if (requiresNoUpdateRequest && hasUpdateRequest) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "No update request was expected for NominationDetail [%s]".formatted(
                NominationDetailDto.fromNominationDetail(nominationDetail).nominationId().id()
            )
        );
      }

    }

    return true;
  }

}
