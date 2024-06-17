package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.interceptorutil.NominationInterceptorUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Component
public class CanViewNominationPostSubmissionInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      CanViewNominationPostSubmission.class
  );

  private final TeamQueryService teamQueryService;

  private final NominationRoleService nominationRoleService;

  private final UserDetailService userDetailService;

  private final NominationDetailService nominationDetailService;

  @Autowired
  public CanViewNominationPostSubmissionInterceptor(TeamQueryService teamQueryService,
                                                    NominationRoleService nominationRoleService,
                                                    UserDetailService userDetailService,
                                                    NominationDetailService nominationDetailService) {
    this.teamQueryService = teamQueryService;
    this.nominationRoleService = nominationRoleService;
    this.userDetailService = userDetailService;
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

      var nominationDetail = nominationDetailService.getPostSubmissionNominationDetail(nominationId)
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.FORBIDDEN,
              "No post submission nomination exists with for nomination id [%s]".formatted(nominationId)
          ));

      var user = userDetailService.getUserDetail();

      var canViewNominationAsLicensingAuthority = teamQueryService.userHasAtLeastOneStaticRole(
          user.wuaId(),
          TeamType.REGULATOR,
          Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
      );

      var canViewNominationAsConsultee = teamQueryService.userHasAtLeastOneStaticRole(
          user.wuaId(),
          TeamType.CONSULTEE,
          Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT)
      );

      var canViewNominationAsRegulator = canViewNominationAsLicensingAuthority || canViewNominationAsConsultee;

      if (canViewNominationAsRegulator) {
        return true;
      } else {

        var canViewNominationAsApplicant = nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
            user.wuaId(),
            nominationDetail,
            Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
        );

        if (canViewNominationAsApplicant) {
          return true;
        } else {
          throw new ResponseStatusException(
              HttpStatus.FORBIDDEN,
              """
                  User with ID %s does not have the required role in the licensing authority or consultee team or is
                  not part of the applicant group team for nomination with ID %s
              """
                  .formatted(user.wuaId(), nominationId.id())
          );
        }
      }
    }

    return true;
  }
}