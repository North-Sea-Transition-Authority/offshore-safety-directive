package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TeamMemberViewTest {

  @Test
  void getDisplayName() {
    var teamMemberView = TeamMemberViewUtil.builder().build();
    assertThat(teamMemberView.getDisplayName()).isEqualTo("Mr Forename Surname");
  }

  @Test
  void getDisplayName_whenTitleIsNull_thenExcludeFromDisplayName() {
    var teamMemberView = TeamMemberViewUtil.builder().build();

    var teamMemberViewWithoutTitle = new TeamMemberView(teamMemberView.webUserAccountId(), null, teamMemberView.firstName(),
        teamMemberView.middleInitials(), teamMemberView.lastName(), teamMemberView.contactEmail(),
        teamMemberView.contactNumber(), teamMemberView.teamRoles());

    assertThat(teamMemberViewWithoutTitle.getDisplayName()).isEqualTo("Forename Surname");
  }
}