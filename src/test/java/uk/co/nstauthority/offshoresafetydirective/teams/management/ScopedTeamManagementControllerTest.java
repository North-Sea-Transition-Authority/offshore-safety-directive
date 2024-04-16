package uk.co.nstauthority.offshoresafetydirective.teams.management;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeReference;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = ScopedTeamManagementController.class)
class ScopedTeamManagementControllerTest extends AbstractControllerTest {

  private static ServiceUserDetail invokingUser;

  @BeforeAll
  static void setUp() {
    invokingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1L)
        .build();
  }

  @Test
  void renderCreateNewOrganisationGroupTeam() throws Exception {
    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ScopedTeamManagementController.class).renderCreateNewOrganisationGroupTeam(null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderCreateNewOrganisationGroupTeam_noAccess() throws Exception {
    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ScopedTeamManagementController.class).renderCreateNewOrganisationGroupTeam(null)))
            .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void handleCreateNewOrganisationGroupTeam() throws Exception {
    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(50)
        .build();

    var newTeam = new Team(UUID.randomUUID());

    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    when(portalOrganisationGroupQueryService.findOrganisationById(eq(50), any()))
        .thenReturn(Optional.of(organisationGroup));

    when(teamManagementService.createScopedTeam(eq(organisationGroup.name()), eq(TeamType.ORGANISATION_GROUP), refEq(TeamScopeReference.from("50", "ORGANISATION_GROUP"))))
        .thenReturn(newTeam);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrganisationGroupTeam(null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("orgGroupId", "50"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(newTeam.getId(), null))));
  }

  @Test
  void handleCreateNewOrganisationGroupTeam_invalidForm() throws Exception {
    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrganisationGroupTeam(null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("orgGroupId", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/teamManagement/scoped/createOrganisationTeam"));

    verify(teamManagementService, never()).createScopedTeam(any(), any(), any());
  }

  @Test
  void handleCreateNewOrganisationGroupTeam_whenTeamAlreadyExists_thenUserRedirected() throws Exception {

    var orgGroup = new OrganisationGroup();
    orgGroup.setOrganisationGroupId(50);

    var existingTeam = new Team(UUID.randomUUID());

    when(teamQueryService.getScopedTeam(
        eq(TeamType.ORGANISATION_GROUP),
        refEq(TeamScopeReference.from("50", "ORGANISATION_GROUP"))
    ))
        .thenReturn(Optional.of(existingTeam));

    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrganisationGroupTeam(null, null)))
            .with(csrf())
            .with(user(invokingUser))
            .param("orgGroupId", "50")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(existingTeam.getId(), null))));

    verify(teamManagementService, never()).createScopedTeam(any(), any(), any());
  }

  @SecurityTest
  void handleCreateNewOrganisationGroupTeam_noAccess() throws Exception {
    when(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(ScopedTeamManagementController.class).handleCreateNewOrganisationGroupTeam(null, null)))
            .with(csrf())
            .with(user(invokingUser))
            .param("orgGroupId", ""))
        .andExpect(status().isForbidden()); // No redirect to next page

    verify(teamManagementService, never()).createScopedTeam(any(), any(), any());
  }

  @Test
  void searchOrganisationGroups() throws Exception {
    when(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    var orgGroup1 = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(1)
        .withName("SHELL one")
        .build();

    var orgGroup2 = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(2)
        .withName("SHELL two")
        .build();

    when(portalOrganisationGroupQueryService.queryOrganisationByName(eq("shell"), any()))
        .thenReturn(List.of(orgGroup2, orgGroup1));

    mockMvc.perform(get(ReverseRouter.route(on(ScopedTeamManagementController.class).searchOrganisationGroups("shell")))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"SHELL one"}, {"id":"2","text":"SHELL two"}]}
         """));
  }

}
