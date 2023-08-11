package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IndustryTeamRoleTest {

  @Test
  void getRoleFromString_whenInvalid_thenEmpty() {
    assertThat(IndustryTeamRole.getRoleFromString("unknown")).isEmpty();
  }

  @Test
  void getRoleFromString_whenValid_andLowercase_thenPresent() {
    var role = IndustryTeamRole.NOMINATION_VIEWER.name().toLowerCase();
    assertThat(IndustryTeamRole.getRoleFromString(role))
        .contains(IndustryTeamRole.NOMINATION_VIEWER);
  }

  @Test
  void getRoleFromString_whenValid_andExactCase_thenPresent() {
    var role = IndustryTeamRole.NOMINATION_VIEWER.name();
    assertThat(IndustryTeamRole.getRoleFromString(role))
        .contains(IndustryTeamRole.NOMINATION_VIEWER);
  }
}