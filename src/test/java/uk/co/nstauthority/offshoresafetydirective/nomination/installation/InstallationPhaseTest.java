package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class InstallationPhaseTest {

  @ParameterizedTest
  @EnumSource(InstallationPhase.class)
  void valueOfOrNull_whenFromEnum_allValuesReturnNonNull(InstallationPhase inputInstallationPhase) {
    var resultingInstallationPhase = InstallationPhase.valueOfOrNull(inputInstallationPhase.name());
    assertThat(resultingInstallationPhase).isEqualTo(inputInstallationPhase);
  }

  @Test
  void valueOfOrNull_whenUnknownValue_thenNullReturned() {
    var resultingInstallationPhase = InstallationPhase.valueOfOrNull("NOT A VALUE");
    assertThat(resultingInstallationPhase).isNull();
  }

}