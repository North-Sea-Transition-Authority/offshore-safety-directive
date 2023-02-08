package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.fivium.energyportalapi.generated.types.MechanicalStatus;

class WellboreMechanicalStatusTest {

  @Test
  void fromPortalMechanicalStatus_whenNoMatch_thenNullReturned() {
    var resultingWellboreMechanicalStatus = WellboreMechanicalStatus.fromPortalMechanicalStatus(null);
    assertThat(resultingWellboreMechanicalStatus).isNull();
  }

  @ParameterizedTest
  @MethodSource("getOsdKnownMechanicalStatusInputs")
  void fromPortalMechanicalStatus_whenMatch_thenWellboreMechanicalStatusReturned(MechanicalStatus mechanicalStatus) {
    var resultingWellboreMechanicalStatus = WellboreMechanicalStatus.fromPortalMechanicalStatus(mechanicalStatus);
    assertThat(resultingWellboreMechanicalStatus.getPortalMechanicalStatus()).isEqualTo(mechanicalStatus);
  }

  private static Stream<Arguments> getOsdKnownMechanicalStatusInputs() {
    return Arrays.stream(WellboreMechanicalStatus.values())
        .map(WellboreMechanicalStatus::getPortalMechanicalStatus)
        .map(Arguments::of);
  }

}