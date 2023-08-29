package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class ConsulteeTeamServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private TeamMemberRoleService teamMemberRoleService;

  @InjectMocks
  @SpyBean
  private ConsulteeTeamService consulteeTeamService;

  @Test
  void getTeam_whenMatch_thenReturnTeam() {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    var teamId = team.toTeamId();

    when(teamService.getTeam(teamId, TeamType.CONSULTEE)).thenReturn(Optional.of(team));

    assertThat(consulteeTeamService.getTeam(teamId)).contains(team);
    verify(teamService, times(1)).getTeam(teamId, team.getTeamType());
  }

  @Test
  void getTeam_whenNoMatch_thenEmptyOptional() {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    var teamId = team.toTeamId();

    when(teamService.getTeam(teamId, TeamType.CONSULTEE)).thenReturn(Optional.empty());

    assertThat(consulteeTeamService.getTeam(teamId)).isEmpty();
    verify(teamService, times(1)).getTeam(teamId, team.getTeamType());
  }

  @Test
  void isAccessManager_whenAccessManager_thenTrue() {

    var teamId = new TeamId(UUID.randomUUID());
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(ConsulteeTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    assertTrue(consulteeTeamService.isAccessManager(teamId, user));
  }

  @Test
  void isAccessManager_whenAccessManager_thenFalse() {

    var teamId = new TeamId(UUID.randomUUID());
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(ConsulteeTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(false);

    assertFalse(consulteeTeamService.isAccessManager(teamId, user));
  }

  @Test
  void addUserTeamRoles_verifyRepositoryInteractions() {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();
    var consulteeRoles = Set.of(
        ConsulteeTeamRole.ACCESS_MANAGER
    );

    consulteeTeamService.addUserTeamRoles(team, userToAdd, consulteeRoles);

    var rolesAsStrings = consulteeRoles
        .stream()
        .map(ConsulteeTeamRole::name)
        .collect(Collectors.toSet());

    verify(teamMemberRoleService, times(1)).addUserTeamRoles(team, userToAdd, rolesAsStrings);
  }

  @Test
  void getTeamsForUser() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = TeamTestUtil.Builder().build();

    when(teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.CONSULTEE))
        .thenReturn(List.of(team));

    var result = consulteeTeamService.getTeamsForUser(user);

    assertThat(result)
        .containsExactly(team);
  }

  @Test
  void isMemberOfConsulteeTeam_isPresent() {
      var user = ServiceUserDetailTestUtil.Builder().build();
      var team = TeamTestUtil.Builder().build();

      when(teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.CONSULTEE))
          .thenReturn(List.of(team));

      var memberOfConsulteeTeam = consulteeTeamService.isMemberOfConsulteeTeam(user);
      assertTrue(memberOfConsulteeTeam);
  }

  @Test
  void isMemberOfConsulteeTeam_isEmpty() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.CONSULTEE))
        .thenReturn(List.of());

    var memberOfConsulteeTeam = consulteeTeamService.isMemberOfConsulteeTeam(user);
    assertFalse(memberOfConsulteeTeam);
  }
}
