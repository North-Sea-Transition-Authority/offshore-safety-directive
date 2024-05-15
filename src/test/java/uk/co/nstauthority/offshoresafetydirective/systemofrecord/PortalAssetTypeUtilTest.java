package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;

class PortalAssetTypeUtilTest {

  @Test
  void getEnumPhaseClass_installationPhase() {
    assertThat(PortalAssetTypeUtil.getEnumPhaseClass(PortalAssetType.INSTALLATION))
        .isEqualTo(InstallationPhase.class);
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, names = {"WELLBORE", "SUBAREA"})
  void getEnumPhaseClass_wellPhase(PortalAssetType portalAssetType) {
    assertThat(PortalAssetTypeUtil.getEnumPhaseClass(portalAssetType))
        .isEqualTo(WellPhase.class);
  }

}