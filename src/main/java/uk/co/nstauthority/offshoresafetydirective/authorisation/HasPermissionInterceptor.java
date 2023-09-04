package uk.co.nstauthority.offshoresafetydirective.authorisation;

import com.google.common.collect.Sets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Set;
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
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Component
public class HasPermissionInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasPermission.class
  );

  private final UserDetailService userDetailService;

  private final PermissionService permissionService;

  @Autowired
  public HasPermissionInterceptor(UserDetailService userDetailService, PermissionService permissionService) {
    this.userDetailService = userDetailService;
    this.permissionService = permissionService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {

      var requiredPermissions = Sets.newHashSet(
          ((HasPermission) getAnnotation(handlerMethod, HasPermission.class)).permissions()
      );

      var user = userDetailService.getUserDetail();

      if (!permissionService.hasPermission(user, requiredPermissions)) {

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
