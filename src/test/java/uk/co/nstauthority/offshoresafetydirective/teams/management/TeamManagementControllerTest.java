package uk.co.nstauthority.offshoresafetydirective.teams.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.management.form.AddMemberFormValidator;
import uk.co.nstauthority.offshoresafetydirective.teams.management.form.MemberRolesFormValidator;
import uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamTypeView;
import uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamView;

@SuppressWarnings({"unchecked", "DataFlowIssue"})
@ContextConfiguration(classes = TeamManagementController.class)
class TeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private MemberRolesFormValidator memberRolesFormValidator;

  @MockBean
  private AddMemberFormValidator addMemberFormValidator;

  @MockBean
  private EnergyPortalConfiguration energyPortalConfiguration;

  private static Team regTeam;
  private static Team organisationTeam;
  private static TeamMemberView regTeamMemberView;
  private static ServiceUserDetail invokingUser;

  @BeforeAll
  static void setUp() {
    regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeam.setName("reg team one");

    organisationTeam = new Team(UUID.randomUUID());
    organisationTeam.setTeamType(TeamType.ORGANISATION_GROUP);
    organisationTeam.setName("org team");

    regTeamMemberView = new TeamMemberView(
        1L,
        "Ms",
        "Test",
        "User",
        "test@example.com",
        "020123456",
        regTeam.getId(),
        List.of(Role.TEAM_MANAGER)
    );

    invokingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1L)
        .build();
  }

  @Test
  void renderTeamTypeList() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser.wuaId()))
        .thenReturn(Set.of(TeamType.ORGANISATION_GROUP, TeamType.REGULATOR));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamTypeViews = (List<TeamTypeView>) modelAndView.getModel().get("teamTypeViews");

    assertThat(teamTypeViews)
        .extracting(TeamTypeView::teamTypeName)
        .containsExactly(TeamType.REGULATOR.getDisplayName(), TeamType.ORGANISATION_GROUP.getDisplayName());
  }

  @Test
  void renderTeamTypeList_singeTypeRedirects() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser.wuaId()))
        .thenReturn(Set.of(TeamType.ORGANISATION_GROUP));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.ORGANISATION_GROUP.getUrlSlug(), null))));
  }

  @SecurityTest
  void renderTeamTypeList_regWithOrgManageCanSeeOrgTeams() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser.wuaId()))
        .thenReturn(Set.of(TeamType.REGULATOR));

    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamTypeViews = (List<TeamTypeView>) modelAndView.getModel().get("teamTypeViews");

    assertThat(teamTypeViews)
        .extracting(TeamTypeView::teamTypeName)
        .containsExactly(TeamType.REGULATOR.getDisplayName(), TeamType.ORGANISATION_GROUP.getDisplayName());
  }

  @SecurityTest
  void renderTeamTypeList_noManageableTeams() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser.wuaId()))
        .thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTeamsOfType_staticTeamRedirectsToSingleInstance() throws Exception {
    when(teamManagementService.getStaticTeamOfTypeUserIsMemberOf(TeamType.REGULATOR, invokingUser.wuaId()))
        .thenReturn(Optional.of(regTeam));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.REGULATOR.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null))));
  }

  @Test
  void renderTeamsOfType_singleScopedTeamRedirectsToInstance() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.ORGANISATION_GROUP, invokingUser.wuaId()))
        .thenReturn(Set.of(organisationTeam));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.ORGANISATION_GROUP.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(organisationTeam.getId(), null))));
  }

  @Test
  void renderTeamsOfType_scopedTeamReturnList() throws Exception {

    var firstOrganisationTeamByName = new Team(UUID.randomUUID());
    firstOrganisationTeamByName.setTeamType(TeamType.ORGANISATION_GROUP);
    firstOrganisationTeamByName.setName("a team name");

    var secondOrganisationTeamByName = new Team(UUID.randomUUID());
    secondOrganisationTeamByName.setTeamType(TeamType.ORGANISATION_GROUP);
    secondOrganisationTeamByName.setName("b team name");

    var thirdOrganisationTeamByName = new Team(UUID.randomUUID());
    thirdOrganisationTeamByName.setTeamType(TeamType.ORGANISATION_GROUP);
    thirdOrganisationTeamByName.setName("C team name");

    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.ORGANISATION_GROUP, invokingUser.wuaId()))
        .thenReturn(Set.of(secondOrganisationTeamByName, thirdOrganisationTeamByName, firstOrganisationTeamByName));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class)
        .renderTeamsOfType(TeamType.ORGANISATION_GROUP.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamTypeViews = (List<TeamView>) modelAndView.getModel().get("teamViews");

    assertThat(teamTypeViews)
        .extracting(TeamView::teamName)
        .containsExactly(
            firstOrganisationTeamByName.getName(),
            secondOrganisationTeamByName.getName(),
            thirdOrganisationTeamByName.getName()
        );
  }

  @SecurityTest
  void renderTeamsOfType_noManageableTeams() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.ORGANISATION_GROUP, invokingUser.wuaId()))
        .thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.ORGANISATION_GROUP.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderTeamsOfType_noManageableTeams_orgAdminNotForbidden() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.ORGANISATION_GROUP, invokingUser.wuaId()))
        .thenReturn(Set.of());

    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class)
        .renderTeamsOfType(TeamType.ORGANISATION_GROUP.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var createNewInstanceUrl = (String) modelAndView.getModel().get("createNewInstanceUrl");

    assertThat(createNewInstanceUrl)
        .isEqualTo(TeamType.ORGANISATION_GROUP.getCreateNewInstanceRoute());
  }

  @Test
  public void renderTeamsOfType_singleScopedTeamRedirectsToInstance_notOrgAdmin() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.ORGANISATION_GROUP, invokingUser.wuaId()))
        .thenReturn(Set.of(organisationTeam));

    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.ORGANISATION_GROUP.getUrlSlug(), null)))
            .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(organisationTeam.getId(), null))));
  }

  @Test
  public void renderTeamsOfType_singleScopedTeamRedirectsToInstance_isOrgAdmin() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.ORGANISATION_GROUP, invokingUser.wuaId()))
        .thenReturn(Set.of(organisationTeam));

    when(teamQueryService.userHasStaticRole(invokingUser.wuaId(), TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.ORGANISATION_GROUP.getUrlSlug(), null)))
            .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/teamManagement/teamInstances"))
        .andExpect(model().attributeExists("teamViews"));
  }

  @Test
  void renderTeamMemberList_whenNotMemberOfTeam_thenForbidden() throws Exception {

    var team = regTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    when(teamManagementService.isMemberOfTeam(team, invokingUser.wuaId()))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTeamMemberList_whenMemberOfTeam_thenOk() throws Exception {

    var team = regTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    when(teamManagementService.isMemberOfTeam(team, invokingUser.wuaId()))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderTeamMemberList_whenOrganisationTeam_andNotMemberOfTeam_andUserHasManageAnyOrganisationRole_thenOk() throws Exception {

    // GIVEN an organisation team
    var team = organisationTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    // AND the invoking user is not a direct member
    when(teamManagementService.isMemberOfTeam(team, invokingUser.wuaId()))
        .thenReturn(false);

    // WHEN the invoking user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM in the regulator team
    when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser.wuaId()))
        .thenReturn(true);

    // THEN the invoking user will be able to view the team
    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderTeamMemberList_whenOrganisationTeam_andNotMemberOfTeam_andUserWithoutManageAnyOrganisationRole_thenForbidden() throws Exception {

    // GIVEN an organisation team
    var team = organisationTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    // AND the invoking user is not a direct member
    when(teamManagementService.isMemberOfTeam(team, invokingUser.wuaId()))
        .thenReturn(false);

    // WHEN the invoking user does not have the CREATE_MANAGE_ANY_ORGANISATION_TEAM in the regulator team
    when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser.wuaId()))
        .thenReturn(false);

    // THEN the invoking user will not be able to view the team
    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class, mode = EnumSource.Mode.EXCLUDE, names = "ORGANISATION_GROUP")
  void renderTeamMemberList_whenNotOrganisationTeam_andNotMemberOfTeam_andCanManageAnyOrganisationRole_thenForbidden(TeamType nonOrganisationTeamType) throws Exception {

    // GIVEN an non-organisation team
    var team = new Team(UUID.randomUUID());
    team.setTeamType(nonOrganisationTeamType);

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    // AND the invoking user is not a direct member
    when(teamManagementService.isMemberOfTeam(team, invokingUser.wuaId()))
        .thenReturn(false);

    // WHEN the invoking user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM in the regulator team
    when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser.wuaId()))
        .thenReturn(true);

    // THEN the invoking user will not be able to view the team
    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTeamMemberList_whenIsMemberOfTeamAndTeamManager_thenAssetModelProperties() throws Exception {

    when(teamManagementService.canManageTeam(regTeam, invokingUser.wuaId()))
        .thenReturn(true);

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.isMemberOfTeam(regTeam, invokingUser.wuaId()))
        .thenReturn(true);

    when(teamManagementService.getTeamMemberViewsForTeam(regTeam))
        .thenReturn(List.of(regTeamMemberView));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/teamManagement/teamMembers"))
        .andExpect(model().attribute("teamName", regTeam.getName()))
        .andExpect(model().attribute("teamMemberViews", List.of(regTeamMemberView)))
        .andExpect(model().attribute("canManageTeam", true))
        .andExpect(model().attribute(
            "addMemberUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(regTeam.getId(), null))
        ))
        .andExpect(model().attribute("rolesInTeam", regTeam.getTeamType().getAllowedRoles()));
  }

  @Test
  void renderTeamMemberList_whenIsMemberOfTeamAndNotTeamManager_thenAssetModelProperties() throws Exception {

    when(teamManagementService.canManageTeam(regTeam, invokingUser.wuaId()))
        .thenReturn(false);

    when(teamManagementService.isMemberOfTeam(regTeam, invokingUser.wuaId()))
        .thenReturn(true);

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getTeamMemberViewsForTeam(regTeam))
        .thenReturn(List.of(regTeamMemberView));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/teamManagement/teamMembers"))
        .andExpect(model().attribute("teamName", regTeam.getName()))
        .andExpect(model().attribute("teamMemberViews", List.of(regTeamMemberView)))
        .andExpect(model().attribute("canManageTeam", false))
        .andExpect(model().attribute(
            "addMemberUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(regTeam.getId(), null))
        ));
  }

  @Test
  void renderTeamMemberList_noTeamFound() throws Exception {
    var nonExistentTeamId = UUID.randomUUID();
    when(teamManagementService.getTeam(nonExistentTeamId))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(nonExistentTeamId, null)))
        .with(user(invokingUser)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderTeamMemberList_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderAddMemberToTeam() throws Exception {
    when(teamManagementService.getTeam(organisationTeam.getId()))
        .thenReturn(Optional.of(organisationTeam));

    when(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION_GROUP, invokingUser.wuaId()))
        .thenReturn(List.of(organisationTeam));

    when(energyPortalConfiguration.registrationUrl())
        .thenReturn("https://example.com");

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(organisationTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var registerUrl = (String) modelAndView.getModel().get("registerUrl");

    assertThat(registerUrl)
        .isEqualTo("https://example.com");
  }

  @Test
  void renderAddMemberToTeam_noTeamFound() throws Exception {
    var nonExistentTeamId = UUID.randomUUID();
    when(teamManagementService.getTeam(nonExistentTeamId))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(nonExistentTeamId, null)))
        .with(user(invokingUser)))
        .andExpect(status().isNotFound());
  }

  @SecurityTest
  void renderAddMemberToTeam_noAccess() throws Exception {
    when(teamManagementService.getTeam(organisationTeam.getId()))
        .thenReturn(Optional.of(organisationTeam));

    when(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION_GROUP, invokingUser.wuaId()))
        .thenReturn(List.of());

    when(energyPortalConfiguration.registrationUrl())
        .thenReturn("https://example.com");

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(organisationTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void handleAddMemberToTeam() throws Exception {
    var epaUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(999)
        .hasSharedAccount(false)
        .canLogin(true)
        .build();

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.of(regTeam));

    when(addMemberFormValidator.isValid(any(), any()))
        .thenReturn(true);

    when(teamManagementService.getEnergyPortalUser("foo"))
        .thenReturn(List.of(epaUser));

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("username", "foo"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(regTeam.getId(), 999L, null))));
  }

  @Test
  void handleAddMemberToTeam_invalidForm() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.of(regTeam));

    when(addMemberFormValidator.isValid(any(), any()))
        .thenReturn(false);

    when(energyPortalConfiguration.registrationUrl())
        .thenReturn("https://example.com");

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser)))
        .andExpect(status().isOk()); // No redirect to next page
  }

  @Test
  void handleAddMemberToTeam_invalidUser() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.of(regTeam));

    when(addMemberFormValidator.isValid(any(), any()))
        .thenReturn(true);

    when(teamManagementService.getEnergyPortalUser("foo"))
        .thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("username", "foo"))
        .andExpect(status().isBadRequest());
  }

  @SecurityTest
  void handleAddMemberToTeam_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("username", "foo"))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderUserTeamRoles() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getTeamMemberView(regTeam, 999L))
        .thenReturn(regTeamMemberView);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(regTeam.getId(), 999L, null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var roleMap = (Map<String, String>) modelAndView.getModel().get("rolesNamesMap");

    assertThat(roleMap)
        .containsExactly(
            Map.entry(Role.TEAM_MANAGER.name(), Role.TEAM_MANAGER.getName()),
            Map.entry(Role.THIRD_PARTY_TEAM_MANAGER.name(), Role.THIRD_PARTY_TEAM_MANAGER.getName()),
            Map.entry(Role.NOMINATION_MANAGER.name(), Role.NOMINATION_MANAGER.getName()),
            Map.entry(Role.APPOINTMENT_MANAGER.name(), Role.APPOINTMENT_MANAGER.getName()),
            Map.entry(Role.VIEW_ANY_NOMINATION.name(), Role.VIEW_ANY_NOMINATION.getName())
        );

    var teamMemberViewModel = (TeamMemberView) modelAndView.getModel().get("teamMemberView");
    assertThat(teamMemberViewModel).isEqualTo(regTeamMemberView);

    var rolesInTeam = (List<Role>) modelAndView.getModel().get("rolesInTeam");

    assertThat(rolesInTeam)
        .containsExactly(
            Role.TEAM_MANAGER,
            Role.THIRD_PARTY_TEAM_MANAGER,
            Role.NOMINATION_MANAGER,
            Role.APPOINTMENT_MANAGER,
            Role.VIEW_ANY_NOMINATION
        );
  }

  @Test
  void renderUserTeamRoles_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.empty());


    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(regTeam.getId(), 999L, null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateUserTeamRoles() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.of(regTeam));

    when(memberRolesFormValidator.isValid(any(), eq(999L), eq(regTeam), any()))
        .thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).updateUserTeamRoles(regTeam.getId(), 999L, null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("roles", "TEAM_MANAGER"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null))));

    verify(teamManagementService).setUserTeamRoles(999L, regTeam, List.of(Role.TEAM_MANAGER));
  }

  @Test
  void updateUserTeamRoles_invalidForm() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.of(regTeam));

    when(memberRolesFormValidator.isValid(any(), eq(999L), eq(regTeam), any()))
        .thenReturn(false);

    when(teamManagementService.getTeamMemberView(regTeam, 999L))
        .thenReturn(regTeamMemberView);

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).updateUserTeamRoles(regTeam.getId(), 999L, null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("roles", "MANAGE_TEAM"))
        .andExpect(status().isOk()); // No redirect to next page

    verify(teamManagementService, never()).setUserTeamRoles(any(), any(), any());
  }

  @Test
  void updateUserTeamRoles_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).updateUserTeamRoles(regTeam.getId(), 999L, null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("roles", "MANAGE_TEAM"))
        .andExpect(status().isForbidden());

    verify(teamManagementService, never()).setUserTeamRoles(any(), any(), any());
  }

  @Test
  void renderRemoveTeamMember() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getTeamMemberView(regTeam, 999L))
        .thenReturn(regTeamMemberView);

    when(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, 999L))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderRemoveTeamMember(regTeam.getId(), 999L)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamMemberViewModel = (TeamMemberView) modelAndView.getModel().get("teamMemberView");
    var teamName = (String) modelAndView.getModel().get("teamName");
    var canRemoveTeamMember = (boolean) modelAndView.getModel().get("canRemoveTeamMember");

    assertThat(teamMemberViewModel).isEqualTo(regTeamMemberView);
    assertThat(teamName).isEqualTo(regTeam.getName());
    assertThat(canRemoveTeamMember).isTrue();
  }

  @Test
  void renderRemoveTeamMember_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderRemoveTeamMember(regTeam.getId(), 999L)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void handleRemoveTeamMember() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.of(regTeam));

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleRemoveTeamMember(regTeam.getId(), 999L)))
        .with(csrf())
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null))));

    verify(teamManagementService).removeUserFromTeam(999L, regTeam);
  }

  @Test
  void handleRemoveTeamMember_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser.wuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleRemoveTeamMember(regTeam.getId(), 999L)))
        .with(csrf())
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());

    verify(teamManagementService, never()).removeUserFromTeam(999L, regTeam);
  }

}