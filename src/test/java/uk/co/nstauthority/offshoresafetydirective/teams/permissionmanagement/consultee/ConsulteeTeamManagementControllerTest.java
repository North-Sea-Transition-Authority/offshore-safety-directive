package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = ConsulteeTeamManagementController.class)
class ConsulteeTeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private ConsulteeTeamService consulteeTeamService;

  @MockBean
  protected PermissionService permissionService;

  @MockBean
  private TeamService teamService;

  @SecurityTest
  void renderMemberList_whenNotAuthenticated_thenRedirectToLogin() throws Exception {
    var teamId = new TeamId(UUID.randomUUID());
    mockMvc.perform(
            get(ReverseRouter.route(on(ConsulteeTeamManagementController.class).renderMemberList(teamId))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void renderMemberList_whenMemberOfTeam_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(ConsulteeTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @Test
  void renderMemberList_whenNoTeamFound_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeTeamManagementController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(ConsulteeTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderMemberList_whenNotAccessManager_assertModelProperties() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withRole(ConsulteeTeamRole.CONSULTEE)
        .build();

    when(teamMemberViewService.getTeamMemberViewsForTeam(team)).thenReturn(List.of(teamMemberView));

    mockMvc.perform(
            get(ReverseRouter.route(on(ConsulteeTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/permissionmanagement/teamMembersPage"))
        .andExpect(model().attribute("pageTitle", "Manage %s".formatted(team.getDisplayName())))
        .andExpect(model().attribute("teamName", team.getDisplayName()))
        .andExpect(model().attribute("teamRoles", ConsulteeTeamRole.values()))
        .andExpect(model().attributeDoesNotExist("addTeamMemberUrl"));
  }

  @Test
  void renderMemberList_whenAccessManager_assertModelProperties() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(consulteeTeamService.isAccessManager(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, ConsulteeTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var teamMemberView = TeamMemberViewTestUtil.Builder().build();
    when(teamMemberViewService.getTeamMemberViewsForTeam(team)).thenReturn(List.of(teamMemberView));

    var canRemoveUsers = true;
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_CONSULTEE_TEAMS)))
        .thenReturn(canRemoveUsers);

    mockMvc.perform(
            get(ReverseRouter.route(on(ConsulteeTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/permissionmanagement/teamMembersPage"))
        .andExpect(model().attribute("pageTitle", "Manage %s".formatted(team.getDisplayName())))
        .andExpect(model().attribute("teamName", team.getDisplayName()))
        .andExpect(model().attribute("teamRoles", ConsulteeTeamRole.values()))
        .andExpect(model().attribute(
            "addTeamMemberUrl",
            ReverseRouter.route(on(ConsulteeAddMemberController.class).renderAddTeamMember(teamId))
        ))
        .andExpect(model().attribute("canRemoveUsers", canRemoveUsers))
        .andExpect(model().attribute("teamMembers", List.of(teamMemberView)));
  }

}