package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsMemberOfTeam;
import uk.co.nstauthority.offshoresafetydirective.logging.LoggerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;

@Component
public class PermissionManagementHandlerInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      IsMemberOfTeam.class
  );

  private final TeamMemberService teamMemberService;

  private final UserDetailService userDetailService;

  @Autowired
  public PermissionManagementHandlerInterceptor(TeamMemberService teamMemberService,
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

      var teamId = extractTeamIdFromRequest(request, handlerMethod);

      var user = userDetailService.getUserDetail();

      if (hasAnnotation(handlerMethod, IsMemberOfTeam.class)) {
        checkIsMemberOfTeam(teamId, user);
      }

    }

    return true;
  }

  public static TeamId extractTeamIdFromRequest(HttpServletRequest httpServletRequest, HandlerMethod handlerMethod) {

    var teamIdParameter = getPathVariableByClass(handlerMethod, TeamId.class);

    if (teamIdParameter.isEmpty()) {
      var errorMessage = "No path variable called teamId found in request";
      LoggerUtil.warn(errorMessage);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @SuppressWarnings("unchecked")
    var pathVariables = (Map<String, String>) httpServletRequest
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    return TeamId.valueOf(pathVariables.get(teamIdParameter.get().getName()));
  }

  private void checkIsMemberOfTeam(TeamId teamId, ServiceUserDetail user) {
    if (!teamMemberService.isMemberOfTeam(teamId, user)) {
      var errorMessage = "User with ID %s is not a member of team with ID %s".formatted(user.wuaId(), teamId.uuid());
      LoggerUtil.warn(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
    }
  }
}