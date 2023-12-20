package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

class CaseEventTypeTest {

  @Test
  void isValidForTeamType_whenValid_thenReturnTrue() {
    var isValidForTeamType = CaseEventType.isValidForTeamType(Collections.singleton(TeamType.REGULATOR), CaseEventType.QA_CHECKS);
    assertTrue(isValidForTeamType);
  }

  @Test
  void isValidForTeamType_whenInvalid_thenReturnFalse() {
    var isValidForTeamType = CaseEventType.isValidForTeamType(Collections.singleton(TeamType.INDUSTRY), CaseEventType.QA_CHECKS);
    assertFalse(isValidForTeamType);
  }
}