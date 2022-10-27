package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TeamMemberViewTest {

  @Test
  void getDisplayName_whenTitle_thenTitleIncludedInDisplayName() {

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTitle("Mr")
        .withFirstName("Forename")
        .withLastName("Surname")
        .build();

    assertThat(teamMemberView.getDisplayName()).isEqualTo("Mr Forename Surname");
  }

  @Test
  void getDisplayName_whenNoTitle_thenTitleIncludedInDisplayName() {

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withTitle(null)
        .withFirstName("Forename")
        .withLastName("Surname")
        .build();

    assertThat(teamMemberView.getDisplayName()).isEqualTo("Forename Surname");
  }
}