package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamView;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ConsulteeEditMemberController.class)
class ConsulteeEditMemberControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private TeamMemberRoleService teamMemberRoleService;

  @MockBean
  ConsulteeTeamMemberEditRolesValidator consulteeTeamMemberEditRolesValidator;

  @MockBean
  protected PermissionService permissionService;

  @MockBean
  private TeamService teamService;

  private Team consulteeTeam;
  private TeamView teamView;
  private ServiceUserDetail accessManager;
  private ServiceUserDetail nonAccessManager;

  @BeforeEach
  void setUp() {
    consulteeTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    teamView = TeamTestUtil.createTeamView(consulteeTeam);
    accessManager = ServiceUserDetailTestUtil.Builder()
        .withWuaId(new Random().nextLong())
        .build();
    nonAccessManager = ServiceUserDetailTestUtil.Builder()
        .withWuaId(new Random().nextLong())
        .build();
  }

  @SecurityTest
  void renderEditMember_whenNotAuthorised_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeEditMemberController.class)
            .renderEditMember(teamView.teamId(), new WebUserAccountId(accessManager.wuaId())))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderEditMember_whenAccessManager_thenOk() throws Exception {

    Set<TeamRole> userRoles = Set.of(ConsulteeTeamRole.ACCESS_MANAGER);

    when(permissionService.hasPermission(accessManager, Set.of(RolePermission.GRANT_ROLES))).thenReturn(true);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamView.teamId(), accessManager,
        Set.of(ConsulteeTeamRole.ACCESS_MANAGER.name()))
    ).thenReturn(true);

    when(teamService.getTeam(teamView.teamId(), ConsulteeEditMemberController.TEAM_TYPE))
        .thenReturn(Optional.of(consulteeTeam));

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withTeamId(teamView.teamId())
        .withWebUserAccountId(accessManager.wuaId())
        .withRoles(userRoles)
        .build();

    when(teamMemberService.getUserAsTeamMembers(accessManager)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamView.teamId(), accessManager)).thenReturn(true);

    when(teamMemberService.getTeamMember(consulteeTeam, teamMember.wuaId()))
        .thenReturn(Optional.of(teamMember));

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withRoles(userRoles)
        .withWebUserAccountId(teamMember.wuaId())
        .build();
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeEditMemberController.class)
            .renderEditMember(teamView.teamId(), teamMember.wuaId())))
            .with(user(accessManager)))
        .andExpect(status().isOk());

  }

  @Test
  void renderEditMember_whenAccessManager_andOk_thenAssertModelProperties() throws Exception {

    Set<TeamRole> userRoles = Set.of(ConsulteeTeamRole.ACCESS_MANAGER);

    when(permissionService.hasPermission(accessManager, Set.of(RolePermission.GRANT_ROLES))).thenReturn(true);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamView.teamId(), accessManager,
        Set.of(ConsulteeTeamRole.ACCESS_MANAGER.name()))
    ).thenReturn(true);

    when(teamService.getTeam(teamView.teamId(), ConsulteeEditMemberController.TEAM_TYPE))
        .thenReturn(Optional.of(consulteeTeam));

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withTeamId(teamView.teamId())
        .withWebUserAccountId(accessManager.wuaId())
        .withRoles(userRoles)
        .build();

    when(teamMemberService.getUserAsTeamMembers(accessManager)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamView.teamId(), accessManager)).thenReturn(true);

    when(teamMemberService.getTeamMember(consulteeTeam, teamMember.wuaId()))
        .thenReturn(Optional.of(teamMember));

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withRoles(userRoles)
        .withWebUserAccountId(teamMember.wuaId())
        .build();
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ConsulteeEditMemberController.class)
            .renderEditMember(teamView.teamId(), teamMember.wuaId())))
            .with(user(accessManager)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    var model = modelAndView.getModelMap();

    var expectedRoles = userRoles.stream()
        .map(TeamRole::name)
        .collect(Collectors.toSet());

    assertThat((TeamMemberRolesForm) model.get("form")).extracting("roles")
        .isEqualTo(expectedRoles);

    assertThat(model).containsKeys("pageTitle")
        .extractingByKeys("roles", "backLinkUrl")
        .containsExactly(
            DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(ConsulteeTeamRole.class),
            ReverseRouter.route(on(ConsulteeTeamManagementController.class).renderMemberList(teamView.teamId()))
        );
  }

  @SecurityTest
  void editMember_whenNotAuthorized_thenIsRedirectedToLoginUrl() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConsulteeEditMemberController.class)
            .editMember(teamView.teamId(), new WebUserAccountId(accessManager.wuaId()), null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void editMember_whenValid_verifyCalls() throws Exception {

    Set<TeamRole> userRoles = Set.of(
        ConsulteeTeamRole.ACCESS_MANAGER,
        ConsulteeTeamRole.CONSULTATION_COORDINATOR
    );

    when(teamService.getTeam(teamView.teamId(), ConsulteeEditMemberController.TEAM_TYPE))
        .thenReturn(Optional.of(consulteeTeam));

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withTeamId(teamView.teamId())
        .withWebUserAccountId(accessManager.wuaId())
        .withRoles(userRoles)
        .build();

    when(teamMemberService.getUserAsTeamMembers(accessManager)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamView.teamId(), accessManager)).thenReturn(true);

    when(teamMemberService.getTeamMember(consulteeTeam, teamMember.wuaId()))
        .thenReturn(Optional.of(teamMember));

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withRoles(userRoles)
        .withWebUserAccountId(teamMember.wuaId())
        .build();
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withTitle("Success")
        .withHeading("Changed roles for %s".formatted(teamMemberView.getDisplayName()))
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ConsulteeEditMemberController.class)
            .editMember(teamView.teamId(), new WebUserAccountId(accessManager.wuaId()), null, null, null)))
            .with(csrf())
            .with(user(accessManager))
            .param("roles", ConsulteeTeamRole.ACCESS_MANAGER.name()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(ConsulteeTeamManagementController.class).renderMemberList(teamView.teamId()))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(teamMemberRoleService).updateUserTeamRoles(consulteeTeam, teamMember.wuaId(),
        Set.of(ConsulteeTeamRole.ACCESS_MANAGER.name()));
  }

}
