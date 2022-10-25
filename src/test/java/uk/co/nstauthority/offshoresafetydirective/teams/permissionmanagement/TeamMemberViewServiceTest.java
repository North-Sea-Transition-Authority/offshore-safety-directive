package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamMemberViewServiceTest {

  @Mock
  private TeamMemberService teamMemberService;

  @InjectMocks
  private TeamMemberViewService teamMemberViewService;

  @Test
  void getTeamMemberViewsForTeam() {
    var team = new Team();
    var wuaId = 100L;

    var tmrBuilder = TeamMemberRoleTestUtil.Builder()
        .withTeam(team)
        .withWebUserAccountId(wuaId);

    var accessManagerTmr = tmrBuilder.withRole(RegulatorTeamRole.ACCESS_MANAGER.name()).build();
    var orgAccessManagerTmr = tmrBuilder.withRole(RegulatorTeamRole.ORGANISATION_ACCESS_MANAGER.name()).build();

    var teamMember = new TeamMember(new WebUserAccountId(wuaId),
        Set.of(RegulatorTeamRole.ACCESS_MANAGER, RegulatorTeamRole.ORGANISATION_ACCESS_MANAGER));

    when(teamMemberService.getTeamMembers(team))
        .thenReturn(List.of(teamMember));

    var result = teamMemberViewService.getTeamMemberViewsForTeam(team);

    assertThat(result).extracting(
        TeamMemberView::webUserAccountId,
        TeamMemberView::title,
        TeamMemberView::firstName,
        TeamMemberView::middleInitials,
        TeamMemberView::lastName,
        TeamMemberView::contactEmail,
        TeamMemberView::contactNumber,
        TeamMemberView::teamRoles
    ).containsExactly(
        Tuple.tuple(
            new WebUserAccountId(wuaId),
            "Mr",
            "John",
            null,
            "Smith",
            "john.smith@test.org",
            null,
            Set.of(RegulatorTeamRole.ACCESS_MANAGER, RegulatorTeamRole.ORGANISATION_ACCESS_MANAGER)
        )
    );

  }
}