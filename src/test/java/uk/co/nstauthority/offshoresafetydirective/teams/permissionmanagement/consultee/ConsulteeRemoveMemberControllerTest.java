package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRemovalService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ConsulteeRemoveMemberController.class})
class ConsulteeRemoveMemberControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private TeamMemberRemovalService teamMemberRemovalService;

  @MockBean
  protected PermissionService permissionService;

  @MockBean
  private TeamService teamService;

  @SecurityTest
  void renderRemoveMember_whenUserIsNotLoggedIn_thenRedirectedToLogin() throws Exception {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    var teamId = team.toTeamId();

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .renderRemoveMember(teamId, webUserAccountIdToAdd))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderRemoveMember_whenAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.CONSULTEE)
        .withRole(ConsulteeTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .renderRemoveMember(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderRemoveMember_whenThirdPartyAccessManager_thenOk() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(false);

    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .renderRemoveMember(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderRemoveMember_whenNoMatchingPrivs_thenForbidden() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withRole(ConsulteeTeamRole.CONSULTEE)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .renderRemoveMember(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderRemoveMember_whenAccessManagerAndUserCanBeRemoved_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.CONSULTEE)
        .withRole(ConsulteeTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var canRemoveTeamMember = true;
    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), ConsulteeTeamRole.ACCESS_MANAGER))
        .thenReturn(canRemoveTeamMember);

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
            .renderRemoveMember(teamId, teamMember.wuaId())))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("teamName", team.getDisplayName()))
        .andExpect(model().attribute("teamMember", teamMemberView))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(ConsulteeTeamManagementController.class)
                .renderMemberList(teamId))
        ))
        .andExpect(model().attribute(
            "removeUrl",
            ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null))
        ))
        .andExpect(model().attribute("canRemoveTeamMember", canRemoveTeamMember))
        .andExpect(model().attribute(
            "pageTitle",
            "Are you sure you want to remove %s from %s?".formatted(teamMemberView.getDisplayName(),
                team.getDisplayName())
        ))
        .andReturn()
        .getModelAndView();
  }

  @Test
  void renderRemoveMember_whenLastAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.CONSULTEE)
        .withRole(ConsulteeTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var canRemoveTeamMember = false;
    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), ConsulteeTeamRole.ACCESS_MANAGER))
        .thenReturn(canRemoveTeamMember);

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
            .renderRemoveMember(teamId, teamMember.wuaId())))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("teamName", team.getDisplayName()))
        .andExpect(model().attribute("teamMember", teamMemberView))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(ConsulteeTeamManagementController.class)
                .renderMemberList(teamId))
        ))
        .andExpect(model().attribute(
            "removeUrl",
            ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null))
        ))
        .andExpect(model().attribute("canRemoveTeamMember", canRemoveTeamMember))
        .andExpect(model().attribute(
            "pageTitle",
            "You are unable to remove %s from %s".formatted(teamMemberView.getDisplayName(),
                team.getDisplayName())
        ))
        .andReturn()
        .getModelAndView();
  }

  @SecurityTest
  void removeMember_whenUserIsNotLoggedIn_thenRedirectedToLogin() throws Exception {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    var teamId = team.toTeamId();

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            post(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .removeMember(teamId, webUserAccountIdToAdd, null)))
                .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void removeMember_whenAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.CONSULTEE)
        .withRole(ConsulteeTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var canRemoveTeamMember = false;
    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), ConsulteeTeamRole.ACCESS_MANAGER))
        .thenReturn(canRemoveTeamMember);

    mockMvc.perform(
            get(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void removeMember_whenThirdPartyAccessManager_thenOk() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var canRemoveTeamMember = false;
    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), ConsulteeTeamRole.ACCESS_MANAGER))
        .thenReturn(canRemoveTeamMember);

    mockMvc.perform(
            post(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void removeMember_whenNoMatchingPrivs_thenForbidden() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withRole(ConsulteeTeamRole.CONSULTEE)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            post(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void removeMember_whenNoTeamFound_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(UUID.randomUUID());

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withRole(ConsulteeTeamRole.CONSULTEE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_CONSULTEE_TEAMS))).thenReturn(true);

    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(
            post(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void removeMember_whenLoggedIn_verifyCalls() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.CONSULTEE)
        .withRole(ConsulteeTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var canRemoveTeamMember = true;
    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), ConsulteeTeamRole.ACCESS_MANAGER))
        .thenReturn(canRemoveTeamMember);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withTitle("Removed member from team")
        .withBannerType(NotificationBannerType.SUCCESS)
        .withContent("%s has been removed from the team".formatted(teamMemberView.getDisplayName()))
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ConsulteeRemoveMemberController.class)
            .removeMember(teamId, teamMember.wuaId(), null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsulteeTeamManagementController.class)
            .renderMemberList(teamId))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(teamMemberRemovalService).removeTeamMember(team, teamMember, ConsulteeTeamRole.ACCESS_MANAGER);

  }
}
