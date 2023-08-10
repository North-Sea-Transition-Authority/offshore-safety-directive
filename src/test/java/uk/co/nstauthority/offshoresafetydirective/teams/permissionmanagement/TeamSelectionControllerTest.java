package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = TeamSelectionController.class)
class TeamSelectionControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamService teamService;

  @MockBean
  private TeamManagementService teamManagementService;

  @SecurityTest
  void renderTeamList_whenNotLoggedIn_thenRedirectedToLoginUrl() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TeamSelectionController.class)
            .renderTeamList(TeamType.CONSULTEE.getUrlSlug()))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void renderTeamList_whenTeamTypeIsNotResolvable_thenRedirectedToTeamTypeSelection() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    mockMvc.perform(get(ReverseRouter.route(on(TeamSelectionController.class).renderTeamList("unknown")))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamTypeSelectionController.class).renderTeamTypeSelection())));
  }

  @Test
  void renderTeamList_whenUserIsNotInAnyTeamsOfType_thenRedirectedToTeamTypeSelection() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    doReturn(user).when(userDetailService).getUserDetail();
    when(teamService.getUserAccessibleTeamsOfType(user, TeamType.REGULATOR)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(TeamSelectionController.class)
            .renderTeamList(TeamType.REGULATOR.getUrlSlug())))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamTypeSelectionController.class).renderTeamTypeSelection())));
  }

  @Test
  void renderTeamList_whenUserIsInOnlyOneTeamOfType_thenRedirectedToTeamManagement() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamType = TeamType.REGULATOR;
    var team = TeamTestUtil.Builder()
        .withTeamType(teamType)
        .build();
    var teamView = TeamTestUtil.createTeamView(team);

    doReturn(user).when(userDetailService).getUserDetail();
    when(teamService.getUserAccessibleTeamsOfType(user, teamType)).thenReturn(List.of(team));
    when(teamManagementService.teamsToTeamViews(List.of(team))).thenReturn(List.of(teamView));

    mockMvc.perform(get(ReverseRouter.route(on(TeamSelectionController.class).renderTeamList(teamType.getUrlSlug())))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(teamView.teamUrl()));
  }

  @Test
  void renderTeamList_whenUserIsInMultipleTeamsOfType_thenOk() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var firstTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var firstTeamView = TeamTestUtil.createTeamView(firstTeam);

    var secondTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var secondTeamView = TeamTestUtil.createTeamView(firstTeam);

    doReturn(user).when(userDetailService).getUserDetail();
    when(teamService.getUserAccessibleTeamsOfType(user, TeamType.REGULATOR)).thenReturn(List.of(firstTeam, secondTeam));
    when(teamManagementService.teamsToTeamViews(List.of(firstTeam, secondTeam)))
        .thenReturn(List.of(firstTeamView, secondTeamView));

    mockMvc.perform(get(ReverseRouter.route(on(TeamSelectionController.class)
            .renderTeamList(TeamType.REGULATOR.getUrlSlug())))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("pageTitle", "Select a team"))
        .andExpect(model().attribute("teamViews", List.of(firstTeamView, secondTeamView)))
        .andExpect(view().name("osd/permissionmanagement/teamSelection"));
  }
}