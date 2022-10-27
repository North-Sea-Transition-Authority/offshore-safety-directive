package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.logging.LoggerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.PermissionManagementHandlerInterceptor;

@Component
public class RegulatorPermissionManagementHandlerInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      RegulatorRolesAllowed.class
  );

  private final TeamMemberService teamMemberService;

  private final UserDetailService userDetailService;

  @Autowired
  public RegulatorPermissionManagementHandlerInterceptor(TeamMemberService teamMemberService,
                                                         UserDetailService userDetailService) {
    this.teamMemberService = teamMemberService;
    this.userDetailService = userDetailService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {

      var teamId = PermissionManagementHandlerInterceptor.extractTeamIdFromRequest(request, handlerMethod);

      var user = userDetailService.getUserDetail();

      if (hasAnnotation(handlerMethod, RegulatorRolesAllowed.class)) {
        checkIsMemberOfTeamWithRole(
            teamId,
            user,
            ((RegulatorRolesAllowed) getAnnotation(handlerMethod, RegulatorRolesAllowed.class)).roles()
        );
      }
    }

    return true;
  }

  private void checkIsMemberOfTeamWithRole(TeamId teamId, ServiceUserDetail user, RegulatorTeamRole[] roles) {

    var roleNames = Arrays.stream(roles)
        .map(RegulatorTeamRole::name)
        .collect(Collectors.toSet());

    if (!teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, roleNames)) {

      var errorMessage = "User with ID %s does not have any role [%s] for team with ID %s"
          .formatted(user.wuaId(), String.join(", ", roleNames), teamId.uuid());

      LoggerUtil.warn(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
    }
  }
}