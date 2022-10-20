package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class TeamMemberViewTest {

  @Test
  void getDisplayName() {
    var teamMemberView = TeamMemberViewUtil.getTeamMemberView(Set.of());
    assertThat(teamMemberView.getDisplayName()).isEqualTo("Mr Forename Surname");
  }

  @Test
  void getDisplayName_whenTitleIsNull_thenExcludeFromDisplayName() {
    var teamMemberView = TeamMemberViewUtil.getTeamMemberView(Set.of());

    var teamMemberViewWithoutTitle = new TeamMemberView(teamMemberView.wuaId(), null, teamMemberView.firstName(),
        teamMemberView.middleInitials(), teamMemberView.lastName(), teamMemberView.contactEmail(),
        teamMemberView.contactNumber(), teamMemberView.teamRoles());

    assertThat(teamMemberViewWithoutTitle.getDisplayName()).isEqualTo("Forename Surname");
  }
}