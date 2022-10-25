package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamMemberServiceTest {

  @Mock
  private TeamMemberRoleRepository teamMemberRoleRepository;

  @InjectMocks
  private TeamMemberService teamMemberService;

  @Test
  void getTeamMembers() {
    var team = new Team();
    team.setTeamType(TeamType.REGULATOR);

    var roleBuilder = TeamMemberRoleTestUtil.Builder()
        .withTeam(team)
        .withWebUserAccountId(1L);

    var teamMemberRoleAccessManager = roleBuilder.withRole(RegulatorTeamRole.ACCESS_MANAGER.name())
        .build();

    var teamMemberRoleOrgAccessManager = roleBuilder.withRole(RegulatorTeamRole.ORGANISATION_ACCESS_MANAGER.name())
        .build();

    when(teamMemberRoleRepository.findAllByTeam(team)).thenReturn(
        List.of(teamMemberRoleAccessManager, teamMemberRoleOrgAccessManager));

    var result = teamMemberService.getTeamMembers(team);

    var expectedTeamMember = new TeamMember(new WebUserAccountId(1L), Set.of(RegulatorTeamRole.ACCESS_MANAGER,
        RegulatorTeamRole.ORGANISATION_ACCESS_MANAGER));

    assertThat(result).containsExactly(expectedTeamMember);
  }
}