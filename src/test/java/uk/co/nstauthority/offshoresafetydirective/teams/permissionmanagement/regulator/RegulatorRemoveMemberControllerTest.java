package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RegulatorRemoveMemberController.class})
class RegulatorRemoveMemberControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private RegulatorTeamMemberRemovalService regulatorTeamMemberRemovalService;

  @MockBean
  private RegulatorTeamService regulatorTeamService;

  @Autowired
  private ApplicationContext applicationContext;

  private Team regulatorTeam;
  private WebUserAccountId wuaId;

  private ServiceUserDetail authenticatedUser;

  @BeforeEach
  void setUp() {
    regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withId(UUID.randomUUID())
        .build();

    wuaId = new WebUserAccountId(Math.abs(new Random().nextLong()));
    authenticatedUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(wuaId.id())
        .build();
  }

  @Test
  void renderRemoveMember_whenNotLoggedIn_thenNotAuthorized() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(RegulatorRemoveMemberController.class)
        .renderRemoveMember(new TeamId(regulatorTeam.getUuid()), wuaId)))
    ).andExpect(status().isUnauthorized());
  }

  @Test
  void renderRemoveMember_whenUserDoesNotHaveRoleInTeam_thenForbidden() throws Exception {

    // Interceptor setup
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(
        new TeamId(any()),
        any(),
        any())
    ).thenReturn(false);

    when(regulatorTeamService.getRegulatorTeamForUser(authenticatedUser)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(RegulatorRemoveMemberController.class)
        .renderRemoveMember(new TeamId(regulatorTeam.getUuid()), wuaId)))
        .with(user(authenticatedUser))
    ).andExpect(status().isForbidden());
  }

  @Test
  void renderRemoveMember_whenUserHasRoleInTeam_thenOk() throws Exception {

    // Interceptor setup
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(
        new TeamId(any()),
        any(),
        any())
    ).thenReturn(true);

    // Endpoint setup
    var teamMember = TeamMemberTestUtil.Builder().build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var canRemoveTeamMembers = true;

    when(regulatorTeamService.getTeam(new TeamId(regulatorTeam.getUuid()))).thenReturn(Optional.of(regulatorTeam));
    when(teamMemberService.getTeamMember(regulatorTeam, wuaId)).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(regulatorTeamMemberRemovalService.canRemoveTeamMember(regulatorTeam, teamMember))
        .thenReturn(canRemoveTeamMembers);

    var teamName = applicationContext.getBean(CustomerConfigurationProperties.class).mnemonic();

    when(regulatorTeamMemberRemovalService.getRemoveScreenPageTitle(teamName, teamMemberView, canRemoveTeamMembers))
        .thenCallRealMethod();

    when(regulatorTeamMemberRemovalService.getAskToRemovePageTitleText(teamName, teamMemberView))
        .thenCallRealMethod();

    var result = mockMvc.perform(get(ReverseRouter.route(on(RegulatorRemoveMemberController.class)
            .renderRemoveMember(new TeamId(regulatorTeam.getUuid()), wuaId)))
            .with(user(authenticatedUser)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(result).isNotNull();

    var model = result.getModelMap();

    assertThat(model).extractingByKeys(
        "teamName",
        "teamMember",
        "backLinkUrl",
        "removeUrl",
        "canRemoveTeamMember"
    ).containsExactly(
        applicationContext.getBean(CustomerConfigurationProperties.class).mnemonic(),
        teamMemberView,
        ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(new TeamId(
            regulatorTeam.getUuid()))),
        ReverseRouter.route(
            on(RegulatorRemoveMemberController.class).removeMember(new TeamId(regulatorTeam.getUuid()), wuaId, null)),
        canRemoveTeamMembers
    );
  }
}