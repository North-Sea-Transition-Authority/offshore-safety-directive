package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamQueryServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamRoleRepository teamRoleRepository;

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

    assertThat(teamQueryService.userHasAtLeastOneStaticRole(1L, TeamType.REGULATOR, Set.of(Role.THIRD_PARTY_TEAM_MANAGER, Role.VIEW_ANY_NOMINATION)))
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


  private TeamRole createTeamRole(Long wuaId, Team team, Role role) {
    var teamRole = new TeamRole(UUID.randomUUID());
    teamRole.setWuaId(wuaId);
    teamRole.setTeam(team);
    teamRole.setRole(role);
    return teamRole;
  }

}
