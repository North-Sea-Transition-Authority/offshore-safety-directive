package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TeamTypeTest {

  @Test
  void getTeamTypeFromUrlSlug_whenSlugIsCorrect_thenPresent() {
    var slug = TeamType.REGULATOR.getUrlSlug();
    var optionalTeamType = TeamType.getTeamTypeFromUrlSlug(slug);
    assertThat(optionalTeamType).isPresent();
  }

  @Test
  void getTeamTypeFromUrlSlug_whenUnknownSlug_thenEmpty() {
    var optionalTeamType = TeamType.getTeamTypeFromUrlSlug("unknown value");
    assertThat(optionalTeamType).isEmpty();
  }
}