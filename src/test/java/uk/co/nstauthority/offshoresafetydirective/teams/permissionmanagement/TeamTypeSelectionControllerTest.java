package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Map;
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

@ContextConfiguration(classes = TeamTypeSelectionController.class)
class TeamTypeSelectionControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamService teamService;

  @MockBean
  private TeamManagementService teamManagementService;

  @SecurityTest
  void renderTeamTypeSelection_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TeamTypeSelectionController.class).renderTeamTypeSelection())))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void renderTeamTypeSelection_whenUserIsNotInAnyTeam() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    doReturn(user).when(userDetailService).getUserDetail();
    when(teamService.getUserAccessibleTeams(user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(TeamTypeSelectionController.class).renderTeamTypeSelection()))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTeamTypeSelection() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = TeamTestUtil.Builder().build();
    Map<TeamType, String> teamTypeRouteMap = Map.of(
        TeamType.REGULATOR,
        "/"
    );

    doReturn(user).when(userDetailService).getUserDetail();
    when(teamService.getUserAccessibleTeams(user)).thenReturn(List.of(team));
    when(teamManagementService.getManageTeamTypeUrls(List.of(team))).thenReturn(teamTypeRouteMap);

    mockMvc.perform(get(ReverseRouter.route(on(TeamTypeSelectionController.class).renderTeamTypeSelection()))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("teamTypeRouteMap", teamTypeRouteMap))
        .andExpect(model().attribute("pageTitle", "Select a team"))
        .andExpect(view().name("osd/permissionmanagement/teamTypeSelection"));
  }
}