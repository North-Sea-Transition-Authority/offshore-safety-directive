package uk.co.nstauthority.offshoresafetydirective.authorisation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.logging.LoggerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

@Component
public class HasPermissionInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasPermission.class
  );

  private final TeamMemberService teamMemberService;

  private final UserDetailService userDetailService;

  @Autowired
  public HasPermissionInterceptor(TeamMemberService teamMemberService,
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

      var user = userDetailService.getUserDetail();

      var teamMembers = teamMemberService.getUserAsTeamMembers(user);

      if (teamMembers == null || teamMembers.isEmpty()) {
        var errorMessage = "User with ID %s is not a member of any team".formatted(user.wuaId());
        LoggerUtil.warn(errorMessage);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
      }

      var requiredPermissions = Arrays.asList(
          ((HasPermission) getAnnotation(handlerMethod, HasPermission.class)).permissions()
      );

      var hasRequiredPermission = teamMembers
          .stream()
          .map(TeamMember::roles)
          .flatMap(Collection::stream)
          .map(TeamRole::getRolePermissions)
          .flatMap(Collection::stream)
          .anyMatch(requiredPermissions::contains);

      if (!hasRequiredPermission) {

        var requiredPermissionNames = requiredPermissions
            .stream()
            .map(RolePermission::name)
            .toList();

        var errorMessage = "User with ID %s doesn't have any of the required permissions %s"
            .formatted(user.wuaId(), StringUtils.join(requiredPermissionNames));

        LoggerUtil.warn(errorMessage);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
      }
    }

    return true;
  }

}
