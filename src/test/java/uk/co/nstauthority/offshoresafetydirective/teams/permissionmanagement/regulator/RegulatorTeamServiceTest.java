package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class RegulatorTeamServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private TeamMemberRoleService teamMemberRoleService;

  @InjectMocks
  private RegulatorTeamService regulatorTeamService;

  @Test
  void getRegulatorTeamForUser_whenUserBelongsToRegulatorTeam_thenReturnRegulatorTeam() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = new Team();
    when(teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.REGULATOR))
        .thenReturn(List.of(team));

    var result = regulatorTeamService.getRegulatorTeamForUser(user);

    assertThat(result).contains(team);
  }

  @Test
  void getRegulatorTeamForUser_whenUserDoesNotBelongToRegulatorTeam_thenReturnEmpty() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    when(teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.REGULATOR)).thenReturn(List.of());

    var result = regulatorTeamService.getRegulatorTeamForUser(user);

    assertThat(result).isEmpty();
  }

  @Test
  void getTeam_whenMatch_thenReturnTeam() {

    var team = TeamTestUtil.Builder().build();

    var teamId = new TeamId(team.getUuid());

    when(teamService.getTeam(teamId, TeamType.REGULATOR)).thenReturn(Optional.of(team));

    assertThat(regulatorTeamService.getTeam(teamId)).contains(team);
    verify(teamService, times(1)).getTeam(teamId, team.getTeamType());
  }

  @Test
  void getTeam_whenNoMatch_thenEmptyOptional() {

    var team = TeamTestUtil.Builder().build();

    var teamId = new TeamId(team.getUuid());

    when(teamService.getTeam(teamId, TeamType.REGULATOR)).thenReturn(Optional.empty());

    assertThat(regulatorTeamService.getTeam(teamId)).isEmpty();
    verify(teamService, times(1)).getTeam(teamId, team.getTeamType());
  }

  @Test
  void isAccessManager_whenAccessManager_thenTrue() {

    var teamId = new TeamId(UUID.randomUUID());
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    assertTrue(regulatorTeamService.isAccessManager(teamId, user));
  }

  @Test
  void isAccessManager_whenAccessManager_thenFalse() {

    var teamId = new TeamId(UUID.randomUUID());
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(false);

    assertFalse(regulatorTeamService.isAccessManager(teamId, user));
  }

  @Test
  void addUserTeamRoles_verifyRepositoryInteractions() {

    var team = TeamTestUtil.Builder().build();
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();
    var regulatorRoles = Set.of(
        RegulatorTeamRole.ACCESS_MANAGER,
        RegulatorTeamRole.ORGANISATION_ACCESS_MANAGER
    );

    regulatorTeamService.addUserTeamRoles(team, userToAdd, regulatorRoles);

    var rolesAsStrings = regulatorRoles
        .stream()
        .map(RegulatorTeamRole::name)
        .collect(Collectors.toSet());

    verify(teamMemberRoleService, times(1)).addUserTeamRoles(team, userToAdd, rolesAsStrings);
  }
}