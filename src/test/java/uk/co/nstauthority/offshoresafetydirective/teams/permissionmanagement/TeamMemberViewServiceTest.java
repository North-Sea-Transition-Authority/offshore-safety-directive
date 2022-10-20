package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamMemberViewServiceTest {

  @InjectMocks
  private TeamMemberViewService teamMemberViewService;

  @Test
  void getUserViewsForTeam() {
    var team = new Team();
    var result = teamMemberViewService.getUserViewsForTeam(team);

    assertThat(result).extracting(
        TeamMemberView::wuaId,
        TeamMemberView::title,
        TeamMemberView::firstName,
        TeamMemberView::middleInitials,
        TeamMemberView::lastName,
        TeamMemberView::contactEmail,
        TeamMemberView::contactNumber,
        TeamMemberView::teamRoles
    ).containsExactly(
        Tuple.tuple(
            1,
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