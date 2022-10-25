package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;

@ContextConfiguration(classes = {RegulatorTeamManagementController.class})
class RegulatorTeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private RegulatorTeamService regulatorTeamService;

  @MockBean
  private UserDetailService userDetailService;

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  void renderMemberList_whenNotAuthorized_thenStatusIsUnauthorized() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList())))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void renderMemberList() throws Exception {

    var serviceUserDetail = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUserDetail);

    var uuid = UUID.randomUUID();
    var team = new Team(uuid);
    team.setTeamType(TeamType.REGULATOR);
    when(regulatorTeamService.getRegulatorTeamForUser(serviceUserDetail)).thenReturn(Optional.of(team));

    var teamMemberView = TeamMemberViewUtil.builder()
        .withRoles(Set.of(RegulatorTeamRole.ACCESS_MANAGER))
        .build();
    when(teamMemberViewService.getTeamMemberViewsForTeam(team)).thenReturn(List.of(teamMemberView));


    var result = mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList()))
                .with(user(serviceUserDetail)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(result).isNotNull();

    var model = result.getModel();
    var mnemonic = applicationContext.getBean(CustomerConfigurationProperties.class).mnemonic();

    assertThat(model).extractingByKeys("pageTitle", "teamName", "teamRoles")
        .containsExactly(
            "Manage %s".formatted(mnemonic),
            mnemonic,
            RegulatorTeamRole.values()
        );

    @SuppressWarnings("unchecked")
    var teamMembers = (List<TeamMemberView>) model.get("teamMembers");
    assertThat(teamMembers).containsExactly(teamMemberView);
  }

  @Test
  void renderMemberList_whenTeamDoesNotExist_thenNotFound() throws Exception {

    var serviceUserDetail = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUserDetail);

    when(regulatorTeamService.getRegulatorTeamForUser(serviceUserDetail)).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList()))
                .with(user(serviceUserDetail)))
        .andExpect(status().isForbidden()); // NOT_FOUND caused by OsdEntityNotFoundException

  }
}