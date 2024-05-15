package uk.co.nstauthority.offshoresafetydirective.teams.management.access;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasAtLeastOneStaticRole;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasStaticRole;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserIsMemberOfStaticTeam;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.management.TeamManagementService;

@ExtendWith(MockitoExtension.class)
class TeamManagementHandlerInterceptorTest {

  @Mock
  private TeamManagementService teamManagementService;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private TeamManagementHandlerInterceptor teamManagementHandlerInterceptor;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HandlerMethod handlerMethod;

  private final ServiceUserDetail invokingUser = new ServiceUserDetail(
      1L,
      2L,
      "Test",
      "User",
      "test@example.com",
      null,
      null
  );


  @Test
  void preHandle_invokingUserHasStaticRole() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserHasStaticRole", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.TEAM_MANAGER))
        .thenReturn(true);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .isTrue();
  }

  @Test
  void preHandle_invokingUserHasStaticRole_noAccess() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserHasStaticRole", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.TEAM_MANAGER))
        .thenReturn(false);

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_invokingUserIsMemberOfStaticTeam() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserIsMemberOfStaticTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(teamQueryService.userHasAtLeastOneStaticRole(
        invokingUser.wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    ))
        .thenReturn(true);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .isTrue();
  }

  @Test
  void preHandle_invokingUserIsMemberOfStaticTeam_noAccess() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserIsMemberOfStaticTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(teamQueryService.userHasAtLeastOneStaticRole(
        invokingUser.wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    ))
        .thenReturn(false);

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_invokingUserHasAtLeastOneStaticRole() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserHasAtLeastOneStaticRole", UUID.class);

    when(handlerMethod.getMethod()).thenReturn(method);

    when(teamQueryService.userHasAtLeastOneStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Set.of(Role.TEAM_MANAGER, Role.NOMINATION_MANAGER)))
        .thenReturn(true);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .isTrue();
  }

  @Test
  void preHandle_invokingUserHasAtLeastOneStaticRole_noAccess() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserHasAtLeastOneStaticRole", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(teamQueryService.userHasAtLeastOneStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Set.of(Role.TEAM_MANAGER, Role.NOMINATION_MANAGER)))
        .thenReturn(false);

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_staticTeam() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", regTeam.getId().toString()));

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.of(regTeam));

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .isTrue();
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_staticTeam_noAccess() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", regTeam.getId().toString()));

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.empty());

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_scopedTeam() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var orgTeam = new Team(UUID.randomUUID());
    orgTeam.setTeamType(TeamType.ORGANISATION_GROUP);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", orgTeam.getId().toString()));

    when(teamManagementService.getTeam(orgTeam.getId()))
        .thenReturn(Optional.of(orgTeam));

    when(teamManagementService.getScopedTeamsOfTypeUserCanManage(orgTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(List.of(orgTeam));

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .isTrue();
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_scopedTeam_noAccess() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var orgTeam = new Team(UUID.randomUUID());
    orgTeam.setTeamType(TeamType.ORGANISATION_GROUP);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", orgTeam.getId().toString()));

    when(teamManagementService.getTeam(orgTeam.getId()))
        .thenReturn(Optional.of(orgTeam));

    when(teamManagementService.getScopedTeamsOfTypeUserCanManage(orgTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(List.of());

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_noTeamIdInPath() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of());

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_malformedTeamIdUuid() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", "not-a-uid"));

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenNoTeamIdInPath_thenBadRequest() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of());

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenMalformedTeamIdUuid_thenBadRequest() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", "not-a-uid"));

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenIsMemberOfTeam_thenOk() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var team = new Team(UUID.randomUUID());

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", team.getId().toString()));

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    when(teamManagementService.isMemberOfTeam(team, invokingUser.wuaId()))
        .thenReturn(true);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod)).isTrue();
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenNotMemberOfTeam_thenForbidden() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var team = new Team(UUID.randomUUID());

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", team.getId().toString()));

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    when(teamManagementService.isMemberOfTeam(team, invokingUser.wuaId()))
        .thenReturn(false);

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenOrganisationTeam_andNotMemberOfTeam_andNotManageAnyOrganisationTeamRole_thenForbidden() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var organisationTeam = new Team(UUID.randomUUID());
    organisationTeam.setTeamType(TeamType.ORGANISATION_GROUP);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", organisationTeam.getId().toString()));

    when(teamManagementService.getTeam(organisationTeam.getId()))
        .thenReturn(Optional.of(organisationTeam));

    when(teamManagementService.isMemberOfTeam(organisationTeam, invokingUser.wuaId()))
        .thenReturn(false);

    when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser.wuaId()))
        .thenReturn(false);

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenOrganisationTeam_andNotMemberOfTeam_andHasManageAnyOrganisationTeamRole_thenOk() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var organisationTeam = new Team(UUID.randomUUID());
    organisationTeam.setTeamType(TeamType.ORGANISATION_GROUP);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", organisationTeam.getId().toString()));

    when(teamManagementService.getTeam(organisationTeam.getId()))
        .thenReturn(Optional.of(organisationTeam));

    when(teamManagementService.isMemberOfTeam(organisationTeam, invokingUser.wuaId()))
        .thenReturn(false);

    when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser.wuaId()))
        .thenReturn(true);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod)).isTrue();
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenConsulteeTeam_andNotMemberOfTeam_andNotManageAnyConsulteeTeamRole_thenForbidden() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var consulteeTeam = new Team(UUID.randomUUID());
    consulteeTeam.setTeamType(TeamType.CONSULTEE);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", consulteeTeam.getId().toString()));

    when(teamManagementService.getTeam(consulteeTeam.getId()))
        .thenReturn(Optional.of(consulteeTeam));

    when(teamManagementService.isMemberOfTeam(consulteeTeam, invokingUser.wuaId()))
        .thenReturn(false);

    when(teamManagementService.userCanManageAnyConsulteeTeam(invokingUser.wuaId()))
        .thenReturn(false);

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenConsulteeTeam_andNotMemberOfTeam_andHasManageAnyConsulteeTeamRole_thenOk() throws Exception {

    when(userDetailService.getUserDetail())
        .thenReturn(invokingUser);

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var consulteeTeam = new Team(UUID.randomUUID());
    consulteeTeam.setTeamType(TeamType.CONSULTEE);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", consulteeTeam.getId().toString()));

    when(teamManagementService.getTeam(consulteeTeam.getId()))
        .thenReturn(Optional.of(consulteeTeam));

    when(teamManagementService.isMemberOfTeam(consulteeTeam, invokingUser.wuaId()))
        .thenReturn(false);

    when(teamManagementService.userCanManageAnyConsulteeTeam(invokingUser.wuaId()))
        .thenReturn(true);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod)).isTrue();
  }

  @Test
  void preHandle_noAnnotation() throws Exception {
    var method = TestController.class.getDeclaredMethod("noAnnotation", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .isTrue();
  }

  @Controller
  static class TestController {

    @GetMapping("/{teamId}/foo")
    @InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.TEAM_MANAGER)
    String invokingUserHasStaticRole(@PathVariable UUID teamId) {
      return "ok";
    }

    @GetMapping("/{teamId}/foo")
    @InvokingUserHasAtLeastOneStaticRole(teamType = TeamType.REGULATOR, roles = { Role.TEAM_MANAGER, Role.NOMINATION_MANAGER})
    String invokingUserHasAtLeastOneStaticRole(@PathVariable UUID teamId) {
      return "ok";
    }

    @GetMapping("/{teamId}/bar")
    @InvokingUserCanManageTeam
    String invokingUserCanManageTeam(@PathVariable UUID teamId) {
      return "ok";
    }

    @GetMapping("/{teamId}/can-view-team")
    @InvokingUserCanViewTeam
    String invokingUserCanViewTeam(@PathVariable UUID teamId) {
      return "ok";
    }

    @GetMapping("/{teamId}/can-view-team")
    @InvokingUserIsMemberOfStaticTeam(teamType = TeamType.REGULATOR)
    String invokingUserIsMemberOfStaticTeam(@PathVariable UUID teamId) {
      return "ok";
    }

    @GetMapping("/{teamId}/baz")
    String noAnnotation(@PathVariable UUID teamId) {
      return "ok";
    }
  }
}
