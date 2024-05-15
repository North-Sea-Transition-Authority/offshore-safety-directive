package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class WellPhaseTest {

  @ParameterizedTest
  @EnumSource(WellPhase.class)
  void valueOfOrNull_whenFromEnum_allValuesReturnNonNull(WellPhase inputWellPhase) {
    var resultingWellPhase = WellPhase.valueOfOrNull(inputWellPhase.name());
    assertThat(resultingWellPhase).isEqualTo(inputWellPhase);
  }

  @Test
  void valueOfOrNull_whenUnknownValue_thenNullReturned() {
    var resultingWellPhase = WellPhase.valueOfOrNull("NOT A VALUE");
    assertThat(resultingWellPhase).isNull();
  }

}