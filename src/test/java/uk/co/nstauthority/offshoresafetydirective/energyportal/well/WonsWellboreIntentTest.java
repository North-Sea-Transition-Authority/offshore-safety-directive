package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class WonsWellboreIntentTest {

  @ParameterizedTest
  @EnumSource(WonsWellboreIntent.class)
  void fromPortalIntent_whenMatches(WonsWellboreIntent wellboreIntent) {
    var resultingWellboreIntent = WonsWellboreIntent.fromPortalIntent(wellboreIntent.getPortalWellboreIntent());
    assertThat(resultingWellboreIntent).isEqualTo(wellboreIntent);
  }

  @Test
  void fromPortalIntent_whenNoMatch() {
    var resultingWellboreIntent = WonsWellboreIntent.fromPortalIntent(null);
    assertThat(resultingWellboreIntent).isNull();
  }

}