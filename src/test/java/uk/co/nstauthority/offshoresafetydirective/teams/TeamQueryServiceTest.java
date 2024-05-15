package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;

@ExtendWith(MockitoExtension.class)
class TeamQueryServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamRoleRepository teamRoleRepository;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  private TeamQueryService teamQueryService;

  @Test
  void userHasStaticRole_hasRole() {
    setupStaticTeamAndRoles(1L, TeamType.REGULATOR, List.of(
        Role.THIRD_PARTY_TEAM_MANAGER,
        Role.TEAM_MANAGER
    ));

    assertThat(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .isTrue();
  }

  @Test
  void userHasStaticRole_doesNotHaveRole() {
    setupStaticTeamAndRoles(1L, TeamType.REGULATOR, List.of(
        Role.THIRD_PARTY_TEAM_MANAGER,
        Role.TEAM_MANAGER
    ));

    assertThat(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.VIEW_ANY_NOMINATION))
        .isFalse();
  }

  @Test
  void userHasStaticRole_invalidRole() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.NOMINATION_EDITOR));
  }

  @Test
  void userHasStaticRole_noTeamInstance() {
    when(teamRepository.findByTeamType(TeamType.REGULATOR)).thenReturn(List.of());
    assertThat(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.VIEW_ANY_NOMINATION))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneStaticRole_hasRole() {
    setupStaticTeamAndRoles(1L, TeamType.REGULATOR, List.of(
        Role.THIRD_PARTY_TEAM_MANAGER,
        Role.TEAM_MANAGER
    ));

    assertThat(teamQueryService.userHasAtLeastOneStaticRole(
        1L,
        TeamType.REGULATOR,
        Set.of(Role.THIRD_PARTY_TEAM_MANAGER, Role.VIEW_ANY_NOMINATION))
    )
        .isTrue();
  }

  @Test
  void userHasAtLeastOneStaticRole_doesNotHaveRole() {
    setupStaticTeamAndRoles(1L, TeamType.REGULATOR, List.of(
        Role.THIRD_PARTY_TEAM_MANAGER,
        Role.TEAM_MANAGER
    ));

    assertThat(teamQueryService.userHasAtLeastOneStaticRole(1L, TeamType.REGULATOR, Set.of(Role.VIEW_ANY_NOMINATION)))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneStaticRole_invalidRole() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> teamQueryService.userHasAtLeastOneStaticRole(1L, TeamType.REGULATOR, Set.of(Role.NOMINATION_EDITOR)));
  }

  @Test
  void userHasAtLeastOneStaticRole_noTeamInstance() {
    when(teamRepository.findByTeamType(TeamType.REGULATOR)).thenReturn(List.of());
    assertThat(teamQueryService.userHasAtLeastOneStaticRole(1L, TeamType.REGULATOR, Set.of(Role.THIRD_PARTY_TEAM_MANAGER)))
        .isFalse();
  }

  @Test
  void userHasScopedRole_hasRole() {
    var scope = TeamScopeReference.from("123", "ORGANISATION_GROUP");
    setupScopedTeamAndRoles(1L, TeamType.ORGANISATION_GROUP, scope, List.of(
        Role.TEAM_MANAGER,
        Role.NOMINATION_VIEWER
    ));

    assertThat(teamQueryService.userHasScopedRole(1L, TeamType.ORGANISATION_GROUP, scope, Role.NOMINATION_VIEWER))
        .isTrue();
  }

  @Test
  void userHasScopedRole_doesNotHaveRole() {
    var scope = TeamScopeReference.from("123", "ORGANISATION_GROUP");
    setupScopedTeamAndRoles(1L, TeamType.ORGANISATION_GROUP, scope, List.of(
        Role.TEAM_MANAGER,
        Role.NOMINATION_VIEWER
    ));

    assertThat(teamQueryService.userHasScopedRole(1L, TeamType.ORGANISATION_GROUP, scope, Role.NOMINATION_EDITOR))
        .isFalse();
  }

  @Test
  void userHasScopedRole_invalidRole() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> teamQueryService.userHasScopedRole(
            1L,
            TeamType.ORGANISATION_GROUP,
            TeamScopeReference.from("1", "ORGANISATION_GROUP"),
            Role.THIRD_PARTY_TEAM_MANAGER
        ));
  }

  @Test
  void userHasScopedRole_noTeamInstance() {
    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", "1"))
        .thenReturn(Optional.empty());

    assertThat(teamQueryService.userHasScopedRole(
        1L,
        TeamType.ORGANISATION_GROUP,
        TeamScopeReference.from("1", "ORGANISATION_GROUP"),
        Role.NOMINATION_VIEWER
    ))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneScopedRole_hasRole() {
    var scope = TeamScopeReference.from("123", "ORGANISATION_GROUP");
    setupScopedTeamAndRoles(1L, TeamType.ORGANISATION_GROUP, scope, List.of(
        Role.TEAM_MANAGER,
        Role.NOMINATION_VIEWER
    ));

    assertThat(teamQueryService.userHasAtLeastOneScopedRole(1L, TeamType.ORGANISATION_GROUP, scope, Set.of(Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)))
        .isTrue();
  }

  @Test
  void userHasAtLeastOneScopedRole_doesNotHaveRole() {
    var scope = TeamScopeReference.from("123", "ORGANISATION_GROUP");
    setupScopedTeamAndRoles(1L, TeamType.ORGANISATION_GROUP, scope, List.of(
        Role.TEAM_MANAGER,
        Role.NOMINATION_VIEWER
    ));

    assertThat(teamQueryService.userHasAtLeastOneScopedRole(1L, TeamType.ORGANISATION_GROUP, scope, Set.of(Role.NOMINATION_EDITOR)))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneScopedRole_invalidRole() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> teamQueryService.userHasScopedRole(
            1L,
            TeamType.ORGANISATION_GROUP,
            TeamScopeReference.from("1", "ORGANISATION_GROUP"),
            Role.VIEW_ANY_NOMINATION
        ));
  }

  @Test
  void userHasAtLeastOneScopedRole_noTeamInstance() {
    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", "1"))
        .thenReturn(Optional.empty());

    assertThat(teamQueryService.userHasAtLeastOneScopedRole(
        1L, TeamType.ORGANISATION_GROUP,
        TeamScopeReference.from("1", "ORGANISATION_GROUP"),
        Set.of(Role.NOMINATION_VIEWER)
    ))
        .isFalse();
  }

  @Test
  void getScopedTeam_whenExists_thenTeamReturned() {

    var scopedReference = TeamScopeReference.from("123", "ORGANISATION_GROUP");

    var expectedTeam = new Team();

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION_GROUP, scopedReference.getType(), scopedReference.getId()))
        .thenReturn(Optional.of(expectedTeam));

    var resultingTeam = teamQueryService.getScopedTeam(TeamType.ORGANISATION_GROUP, scopedReference);

    assertThat(resultingTeam).contains(expectedTeam);
  }

  @Test
  void getScopedTeam_whenNoTeam_thenEmptyOptional() {

    var scopedReference = TeamScopeReference.from("123", "ORGANISATION_GROUP");

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION_GROUP, scopedReference.getType(), scopedReference.getId()))
        .thenReturn(Optional.empty());

    var resultingTeam = teamQueryService.getScopedTeam(TeamType.ORGANISATION_GROUP, scopedReference);

    assertThat(resultingTeam).isEmpty();
  }

  @Test
  void getTeamRolesForUser_whenRoles() {

    var wuaId = 1L;

    var expectedRole = new TeamRole();

    when(teamRoleRepository.findAllByWuaId(wuaId))
        .thenReturn(List.of(expectedRole));

    var resultingRoles = teamQueryService.getTeamRolesForUser(wuaId);

    assertThat(resultingRoles).containsExactly(expectedRole);
  }

  @Test
  void getTeamRolesForUser_whenNoRoles() {

    var wuaId = 1L;

    when(teamRoleRepository.findAllByWuaId(wuaId))
        .thenReturn(List.of());

    var resultingRoles = teamQueryService.getTeamRolesForUser(wuaId);

    assertThat(resultingRoles).isEmpty();
  }

  @Test
  void areRolesValidForTeamType_whenAllRolesValid() {

    var areRolesValid = teamQueryService.areRolesValidForTeamType(
        TeamType.REGULATOR.getAllowedRoles(),
        TeamType.REGULATOR
    );

    assertThat(areRolesValid).isTrue();
  }

  @Test
  void areRolesValidForTeamType_whenSomeRolesValid() {

    var validRoleForRegulatorTeamType = Role.NOMINATION_EDITOR;
    var invalidRoleForRegulatorTeamType = Role.CONSULTATION_PARTICIPANT;

    var areRolesValid = teamQueryService.areRolesValidForTeamType(
        Set.of(validRoleForRegulatorTeamType, invalidRoleForRegulatorTeamType),
        TeamType.REGULATOR
    );

    assertThat(areRolesValid).isFalse();
  }

  @Test
  void areRolesValidForTeamType_whenNoRolesValid() {

    var areRolesValid = teamQueryService.areRolesValidForTeamType(
        Set.of(Role.CONSULTATION_PARTICIPANT),
        TeamType.REGULATOR
    );

    assertThat(areRolesValid).isFalse();
  }

  @Test
  void getTeamsOfTypeUserIsMemberOf_whenUserMemberOfTeamsOfType() {

    var wuaId = 1L;

    var organisationTeam = new Team();
    organisationTeam.setTeamType(TeamType.ORGANISATION_GROUP);

    var organisationTeamRole = new TeamRole();
    organisationTeamRole.setTeam(organisationTeam);

    var nonOganisationTeam = new Team();
    nonOganisationTeam.setTeamType(TeamType.REGULATOR);

    var nonOrganisationTeamRole = new TeamRole();
    nonOrganisationTeamRole.setTeam(nonOganisationTeam);

    when(teamRoleRepository.findAllByWuaId(wuaId))
        .thenReturn(List.of(organisationTeamRole, nonOrganisationTeamRole));

    var resultingTeams = teamQueryService.getTeamsOfTypeUserIsMemberOf(wuaId, TeamType.ORGANISATION_GROUP);

    assertThat(resultingTeams).containsExactly(organisationTeam);
  }

  @Test
  void getTeamsOfTypeUserIsMemberOf_whenUserNotMemberOfTeamTypes() {

    var wuaId = 1L;

    when(teamRoleRepository.findAllByWuaId(wuaId))
        .thenReturn(List.of());

    var resultingTeams = teamQueryService.getTeamsOfTypeUserIsMemberOf(wuaId, TeamType.ORGANISATION_GROUP);

    assertThat(resultingTeams).isEmpty();
  }

  @Test
  void getUserWithStaticRole_whenRoleNotValidForTeamType() {
    assertThatThrownBy(() -> teamQueryService.getUserWithStaticRole(TeamType.REGULATOR, Role.CONSULTATION_MANAGER))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getUserWithStaticRole_whenTeamTypeIsScoped() {
    assertThatThrownBy(() -> teamQueryService.getUserWithStaticRole(TeamType.ORGANISATION_GROUP, Role.TEAM_MANAGER))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getUserWithStaticRole_whenTeamNotFound() {

    when(teamRepository.findByTeamType(TeamType.REGULATOR))
        .thenReturn(List.of());

    var resultingUsers = teamQueryService.getUserWithStaticRole(TeamType.REGULATOR, Role.TEAM_MANAGER);

    assertThat(resultingUsers).isEmpty();

    verifyNoInteractions(teamRoleRepository);
    verifyNoInteractions(energyPortalUserService);
  }

  @Test
  void getUserWithStaticRole_whenNoUserWithRole() {

    var expectedTeam = new Team();

    when(teamRepository.findByTeamType(TeamType.REGULATOR))
        .thenReturn(List.of(expectedTeam));

    when(teamRoleRepository.findAllByTeamAndRole(expectedTeam, Role.TEAM_MANAGER))
        .thenReturn(Set.of());

    var resultingUsers = teamQueryService.getUserWithStaticRole(TeamType.REGULATOR, Role.TEAM_MANAGER);

    assertThat(resultingUsers).isEmpty();

    verifyNoInteractions(energyPortalUserService);
  }

  @Test
  void getUserWithStaticRole_whenUserWithRole() {

    var expectedTeam = new Team();

    when(teamRepository.findByTeamType(TeamType.REGULATOR))
        .thenReturn(List.of(expectedTeam));

    var expectedTeamRole = new TeamRole();
    expectedTeamRole.setWuaId(10L);

    when(teamRoleRepository.findAllByTeamAndRole(expectedTeam, Role.TEAM_MANAGER))
        .thenReturn(Set.of(expectedTeamRole));

    var expectedUser = EnergyPortalUserDtoTestUtil.Builder().build();

    when(energyPortalUserService.findByWuaIds(
        Set.of(new WebUserAccountId(10L)),
        new RequestPurpose("Get users with a certain role in static team")
    ))
        .thenReturn(List.of(expectedUser));

    var resultingUsers = teamQueryService.getUserWithStaticRole(TeamType.REGULATOR, Role.TEAM_MANAGER);

    assertThat(resultingUsers).containsExactly(expectedUser);
  }

  @Test
  void getUsersInScopedTeam_whenTeamTypeIsNotScoped() {

    var nonScopedTeamType = TeamType.REGULATOR;
    var teamReference = TeamScopeReference.from("123", "TEAM_SCOPE");

    assertThatThrownBy(() -> teamQueryService.getUsersInScopedTeam(nonScopedTeamType, teamReference))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getUsersInScopedTeam_whenTeamOfTypeNotFound() {

    var scopedTeamType = TeamType.ORGANISATION_GROUP;
    var teamReference = TeamScopeReference.from("123", "TEAM_SCOPE");

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(scopedTeamType, teamReference.getType(), teamReference.getId()))
        .thenReturn(Optional.empty());

    var userRoles = teamQueryService.getUsersInScopedTeam(scopedTeamType, teamReference);

    assertThat(userRoles).isEmpty();
  }

  @Test
  void getUsersInScopedTeam_whenNoUsersInTeam() {

    var scopedTeamType = TeamType.ORGANISATION_GROUP;
    var teamReference = TeamScopeReference.from("123", "TEAM_SCOPE");

    var expectedTeam = new Team(UUID.randomUUID());

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(scopedTeamType, teamReference.getType(), teamReference.getId()))
        .thenReturn(Optional.of(expectedTeam));

    when(teamRoleRepository.findByTeam(expectedTeam))
        .thenReturn(List.of());

    var userRoles = teamQueryService.getUsersInScopedTeam(scopedTeamType, teamReference);

    assertThat(userRoles).isEmpty();

    verifyNoInteractions(energyPortalUserService);
  }

  @Test
  void getUsersInScopedTeam_whenUsersInTeam() {

    var scopedTeamType = TeamType.ORGANISATION_GROUP;
    var teamReference = TeamScopeReference.from("123", "TEAM_SCOPE");

    var expectedTeam = new Team(UUID.randomUUID());

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(scopedTeamType, teamReference.getType(), teamReference.getId()))
        .thenReturn(Optional.of(expectedTeam));

    var firstUserFirstRole = new TeamRole(UUID.randomUUID());
    firstUserFirstRole.setWuaId(10L);
    firstUserFirstRole.setRole(Role.TEAM_MANAGER);

    var firstUserSecondRole = new TeamRole(UUID.randomUUID());
    firstUserSecondRole.setWuaId(10L);
    firstUserSecondRole.setRole(Role.NOMINATION_VIEWER);

    var secondUser = new TeamRole(UUID.randomUUID());
    secondUser.setWuaId(20L);
    secondUser.setRole(Role.TEAM_MANAGER);

    when(teamRoleRepository.findByTeam(expectedTeam))
        .thenReturn(List.of(firstUserFirstRole, firstUserSecondRole, secondUser));

    var firstEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(10L)
        .build();

    var secondEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(20L)
        .build();

    when(energyPortalUserService.findByWuaIds(
        Set.of(new WebUserAccountId(10L), new WebUserAccountId(20L)),
        new RequestPurpose("Get users in applicant organisation group team")
    ))
        .thenReturn(List.of(firstEnergyPortalUser, secondEnergyPortalUser));

    var userRoles = teamQueryService.getUsersInScopedTeam(scopedTeamType, teamReference);

    assertThat(userRoles)
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                Role.TEAM_MANAGER, Set.of(firstEnergyPortalUser, secondEnergyPortalUser),
                Role.NOMINATION_VIEWER, Set.of(firstEnergyPortalUser)
            )
        );
  }

  private void setupStaticTeamAndRoles(Long wuaId, TeamType teamType, List<Role> roles) {
    var team = new Team(UUID.randomUUID());
    team.setTeamType(teamType);
    var teamRoles = roles.stream()
        .map(role -> createTeamRole(wuaId, team, role))
        .toList();

    when(teamRepository.findByTeamType(teamType))
        .thenReturn(List.of(team));
    when(teamRoleRepository.findByWuaIdAndTeam(wuaId, team))
        .thenReturn(teamRoles);
  }

  private void setupScopedTeamAndRoles(Long wuaId, TeamType teamType, TeamScopeReference scopeRef, List<Role> roles) {
    var team = new Team(UUID.randomUUID());
    team.setScopeType(scopeRef.getType());
    team.setScopeId(scopeRef.getId());
    team.setTeamType(teamType);
    var teamRoles = roles.stream()
        .map(role -> createTeamRole(wuaId, team, role))
        .toList();

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId()))
        .thenReturn(Optional.of(team));
    when(teamRoleRepository.findByWuaIdAndTeam(wuaId, team))
        .thenReturn(teamRoles);
  }

  @Test
  void getScopedTeams_whenNoMatchingTeam() {

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeIdIn(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", Set.of("1")))
        .thenReturn(Set.of());

    var resultingTeams = teamQueryService.getScopedTeams(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", Set.of("1"));

    assertThat(resultingTeams).isEmpty();
  }

  @Test
  void getScopedTeams_whenMatchingTeam() {

    var expectedTeam = new Team(UUID.randomUUID());

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeIdIn(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", Set.of("1")))
        .thenReturn(Set.of(expectedTeam));

    var resultingTeams = teamQueryService.getScopedTeams(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", Set.of("1"));

    assertThat(resultingTeams).containsExactly(expectedTeam);
  }


  private TeamRole createTeamRole(Long wuaId, Team team, Role role) {
    var teamRole = new TeamRole(UUID.randomUUID());
    teamRole.setWuaId(wuaId);
    teamRole.setTeam(team);
    teamRole.setRole(role);
    return teamRole;
  }

}
