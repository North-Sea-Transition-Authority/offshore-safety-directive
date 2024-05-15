package uk.co.nstauthority.offshoresafetydirective.teams.management.access;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasAtLeastOneStaticRole;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasStaticRole;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserIsMemberOfStaticTeam;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.management.TeamManagementService;

@Component
public class TeamManagementHandlerInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      InvokingUserCanManageTeam.class,
      InvokingUserHasStaticRole.class,
      InvokingUserHasAtLeastOneStaticRole.class,
      InvokingUserCanViewTeam.class,
      InvokingUserIsMemberOfStaticTeam.class
  );

  private final TeamManagementService teamManagementService;
  private final UserDetailService userDetailService;
  private final TeamQueryService teamQueryService;

  public TeamManagementHandlerInterceptor(TeamManagementService teamManagementService,
                                          UserDetailService userDetailService, TeamQueryService teamQueryService) {
    this.teamManagementService = teamManagementService;
    this.userDetailService = userDetailService;
    this.teamQueryService = teamQueryService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {
    if (handler instanceof ResourceHttpRequestHandler) {
      return true;
    }

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS
    )) {
      var wuaId = userDetailService.getUserDetail().wuaId();

      if (hasAnnotation(handlerMethod, InvokingUserCanManageTeam.class)) {
        return handleInvokingUserCanManageTeamCheck(request, wuaId);
      }

      if (hasAnnotation(handlerMethod, InvokingUserHasStaticRole.class)) {
        var annotation = (InvokingUserHasStaticRole) getAnnotation(handlerMethod, InvokingUserHasStaticRole.class);
        return handleInvokingUserHasRoleCheck(wuaId, annotation.teamType(), annotation.role());
      }

      if (hasAnnotation(handlerMethod, InvokingUserHasAtLeastOneStaticRole.class)) {
        var annotation = (InvokingUserHasAtLeastOneStaticRole) getAnnotation(
            handlerMethod,
            InvokingUserHasAtLeastOneStaticRole.class
        );
        return handleInvokingUserHasAtLeastOneStaticRoleCheck(
            wuaId,
            annotation.teamType(),
            Arrays.stream(annotation.roles()).collect(Collectors.toSet())
        );
      }

      if (hasAnnotation(handlerMethod, InvokingUserCanViewTeam.class)) {
        return handleInvokingUserCanViewTeam(request, wuaId);
      }

      if (hasAnnotation(handlerMethod, InvokingUserIsMemberOfStaticTeam.class)) {
        var annotation = (InvokingUserIsMemberOfStaticTeam) getAnnotation(
            handlerMethod,
            InvokingUserIsMemberOfStaticTeam.class
        );
        return handleInvokingUserIsMemberOfStaticTeam(wuaId, annotation);
      }
    }
    return true;
  }

  private boolean handleInvokingUserCanManageTeamCheck(HttpServletRequest request, Long wuaId) {

    var team = getTeamFromRequest(request);

    var isScoped = team.getTeamType().isScoped();
    boolean canManageTeam;
    if (isScoped) {
      canManageTeam = teamManagementService.getScopedTeamsOfTypeUserCanManage(team.getTeamType(), wuaId).contains(team);
    } else {
      canManageTeam = teamManagementService.getStaticTeamOfTypeUserCanManage(team.getTeamType(), wuaId).map(
          t -> t.equals(team)).isPresent();
    }

    if (canManageTeam) {
      return true;
    } else {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "wuaId %s does not have manage team role for teamId %s".formatted(wuaId, team.getId()));
    }
  }

  private boolean handleInvokingUserHasRoleCheck(Long wuaId, TeamType teamType, Role role) {
    if (teamQueryService.userHasStaticRole(wuaId, teamType, role)) {
      return true;
    } else {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "wuaId %s does not have static role %s for teamType %s".formatted(wuaId, role, teamType)
      );
    }
  }

  private boolean handleInvokingUserHasAtLeastOneStaticRoleCheck(Long wuaId, TeamType teamType, Set<Role> roles) {
    if (teamQueryService.userHasAtLeastOneStaticRole(wuaId, teamType, roles)) {
      return true;
    } else {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "wuaId %s does not have at least one static role of %s for teamType %s".formatted(wuaId, roles, teamType)
      );
    }
  }

  private boolean handleInvokingUserIsMemberOfStaticTeam(long wuaId, InvokingUserIsMemberOfStaticTeam annotation) {
    var isMemberOfStaticTeam = teamQueryService.userHasAtLeastOneStaticRole(
        wuaId,
        annotation.teamType(),
        new LinkedHashSet<>(annotation.teamType().getAllowedRoles())
    );

    if (isMemberOfStaticTeam) {
      return true;
    } else {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "wuaId %s is not part of static team of type %s".formatted(wuaId, annotation.teamType())
      );
    }
  }

  private boolean handleInvokingUserCanViewTeam(HttpServletRequest request, Long wuaId) {

    var team = getTeamFromRequest(request);

    if (teamManagementService.isMemberOfTeam(team, wuaId)
        || (TeamType.ORGANISATION_GROUP.equals(team.getTeamType()) && userCanManageAnyOrganisationTeam(wuaId))
        || (TeamType.CONSULTEE.equals(team.getTeamType()) && userCanManageAnyConsulteeTeam(wuaId))) {
      return true;
    } else {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "wuaId %s is not a member of team %s".formatted(wuaId, team.getId())
      );
    }
  }

  @SuppressWarnings("unchecked")
  private Team getTeamFromRequest(HttpServletRequest request) {

    var pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    var teamIdString = pathVariables.get("teamId");

    if (teamIdString == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teamId path variable not found in request");
    }

    UUID teamId;

    try {
      teamId = UUID.fromString(teamIdString);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UUID parse error", e);
    }

    return teamManagementService.getTeam(teamId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "teamId %s not found".formatted(teamId)));
  }

  private boolean userCanManageAnyOrganisationTeam(long wuaId) {
    return teamManagementService.userCanManageAnyOrganisationTeam(wuaId);
  }

  private boolean userCanManageAnyConsulteeTeam(long wuaId) {
    return teamManagementService.userCanManageAnyConsulteeTeam(wuaId);
  }
}