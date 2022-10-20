package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class RegulatorTeamServiceTest {

  @Mock
  private TeamService teamService;

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
}