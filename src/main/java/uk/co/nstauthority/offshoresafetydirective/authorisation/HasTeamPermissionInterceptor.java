package uk.co.nstauthority.offshoresafetydirective.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.PermissionManagementHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Component
public class HasTeamPermissionInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasTeamPermission.class
  );

  private final TeamMemberService teamMemberService;
  private final UserDetailService userDetailService;

  @Autowired
  public HasTeamPermissionInterceptor(TeamMemberService teamMemberService, UserDetailService userDetailService) {
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
      var hasTeamPermissionAnnotation = (HasTeamPermission) getAnnotation(handlerMethod, HasTeamPermission.class);

      verifyAtLeastOnePermissionExpected(hasTeamPermissionAnnotation, request);

      var user = userDetailService.getUserDetail();
      var userAsTeamMembers = teamMemberService.getUserAsTeamMembers(user);
      var teamMemberPermissions = getAllPermissions(userAsTeamMembers);

      var hasMatchingPermission = Arrays.stream(hasTeamPermissionAnnotation.anyNonTeamPermissionOf())
          .anyMatch(teamMemberPermissions::contains);

      if (hasMatchingPermission) {
        return true;
      }

      var teamId = PermissionManagementHandlerInterceptor.extractTeamIdFromRequest(request, handlerMethod);

      if (teamMemberService.isMemberOfTeam(teamId, user)) {
        var teamMemberPermissionsInTeam = getPermissionsInTeam(teamId, userAsTeamMembers);
        var hasMatchingTeamPermission = Arrays.stream(hasTeamPermissionAnnotation.anyTeamPermissionOf())
            .anyMatch(teamMemberPermissionsInTeam::contains);

        if (!hasMatchingTeamPermission) {
          var permissionsAsString = Arrays.stream(hasTeamPermissionAnnotation.anyTeamPermissionOf())
              .map(Enum::name)
              .collect(Collectors.joining(","));
          throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              "User [%s] does not have required permissions [%s] in team [%s]".formatted(
                  user.wuaId(),
                  permissionsAsString,
                  teamId.uuid()
              ));
        }
      } else {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User [%s] does not belong to team [%s]".formatted(
            user.wuaId(),
            teamId.uuid()
        ));
      }
    }

    return true;
  }

  private List<RolePermission> getAllPermissions(Collection<TeamMember> teamMembers) {
    return teamMembers.stream()
        .flatMap(teamMember -> teamMember.roles().stream())
        .flatMap(teamRole -> teamRole.getRolePermissions().stream())
        .toList();
  }

  private List<RolePermission> getPermissionsInTeam(TeamId teamId, Collection<TeamMember> teamMembers) {
    return teamMembers.stream()
        .filter(teamMember -> teamMember.teamView().teamId().equals(teamId))
        .flatMap(teamMember -> teamMember.roles().stream())
        .flatMap(teamRole -> teamRole.getRolePermissions().stream())
        .toList();
  }

  private void verifyAtLeastOnePermissionExpected(Annotation permissionAnnotation, HttpServletRequest request) {
    var hasTeamPermission = (HasTeamPermission) permissionAnnotation;
    var permissions = Stream.of(hasTeamPermission.anyNonTeamPermissionOf(), hasTeamPermission.anyTeamPermissionOf())
        .flatMap(Arrays::stream)
        .toList();
    if (permissions.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "No permissions specified on HasTeamPermission annotation for route [%s]".formatted(request.getRequestURI()));
    }
  }

}
