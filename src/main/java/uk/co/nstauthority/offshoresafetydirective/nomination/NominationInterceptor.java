package uk.co.nstauthority.offshoresafetydirective.nomination;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.interceptorutil.NominationInterceptorUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.NominationDetailFetchType;

@Component
public class NominationInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasNominationStatus.class
  );

  private static final Logger LOGGER = LoggerFactory.getLogger(NominationInterceptor.class);

  private final NominationDetailService nominationDetailService;
  private final UserDetailService userDetailService;

  @Autowired
  NominationInterceptor(NominationDetailService nominationDetailService,
                        UserDetailService userDetailService) {
    this.nominationDetailService = nominationDetailService;
    this.userDetailService = userDetailService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {
      var nominationId = NominationInterceptorUtil.extractNominationIdFromRequest(request, handlerMethod);

      NominationDetail nominationDetail = null;
      HasNominationStatus hasNominationStatusAnnotation = getNominationStatusAnnotation(handlerMethod);

      if (hasNominationStatusAnnotation != null) {
        nominationDetail = getNominationDetail(hasNominationStatusAnnotation.fetchType(), nominationId);
        checkNominationStatus(hasNominationStatusAnnotation, nominationDetail);
      }

      // TODO OSDOP-811
//      if (hasNominationPermissionAnnotation != null) {
//        nominationDetail = nominationDetail != null
//            ? nominationDetail
//            : getNominationDetail(NominationDetailFetchType.LATEST, nominationId);
//        checkNominationPermission(hasNominationPermissionAnnotation, nominationDetail);
//      }
    }

    return true;
  }

  private HasNominationStatus getNominationStatusAnnotation(HandlerMethod handlerMethod) {
    HasNominationStatus hasNominationStatusAnnotation = null;

    if (hasAnnotation(handlerMethod, HasNominationStatus.class)) {
      hasNominationStatusAnnotation = (HasNominationStatus) getAnnotation(handlerMethod, HasNominationStatus.class);
    }
    return hasNominationStatusAnnotation;
  }

  // TODO OSDOP-81
//  private HasNominationPermission getNominationPermissionAnnotation(HandlerMethod handlerMethod) {
//    HasNominationPermission hasNominationPermissionAnnotation = null;
//
//    if (hasAnnotation(handlerMethod, HasNominationPermission.class)) {
//      hasNominationPermissionAnnotation = (HasNominationPermission) getAnnotation(handlerMethod, HasNominationPermission.class);
//    }
//    return hasNominationPermissionAnnotation;
//  }

  private NominationDetail getNominationDetail(NominationDetailFetchType fetchType, NominationId nominationId) {
    var nominationDetail = switch (fetchType) {
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

    return nominationDetail;
  }

  // TODO OSDOP-811
//  private void checkNominationPermission(HasNominationPermission annotation, NominationDetail nominationDetail) {
//    List<RolePermission> allowedPermissions = Arrays.asList(annotation.permissions());
//
//    var userHasAllowedPermissionsForApplicantTeam = permissionService.hasPermissionForNomination(
//        nominationDetail,
//        userDetailService.getUserDetail(),
//        allowedPermissions
//    );
//
//    if (!userHasAllowedPermissionsForApplicantTeam) {
//      throw new ResponseStatusException(
//          HttpStatus.FORBIDDEN,
//          "User does not have required permission {%s} in applicants team".formatted(allowedPermissions));
//    }
//  }

  private void checkNominationStatus(HasNominationStatus annotation, NominationDetail nominationDetail) {

    var allowedStatuses = Arrays.asList(annotation.statuses());

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

      LOGGER.warn(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
    }
  }

}
