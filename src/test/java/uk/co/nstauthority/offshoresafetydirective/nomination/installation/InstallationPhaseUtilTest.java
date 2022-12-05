package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class InstallationPhaseUtilTest {

  @Test
  void getInstallationPhasesForNominatedInstallationDetail_whenNullValues_thenEmptyResult() {
    var detail = NominatedInstallationDetailTestUtil.builder()
        .withForAllInstallationPhases(null)
        .withDevelopmentDesignPhase(null)
        .withDevelopmentConstructionPhase(null)
        .withDevelopmentInstallationPhase(null)
        .withDevelopmentCommissioningPhase(null)
        .withDevelopmentProductionPhase(null)
        .withDecommissioningPhase(null)
        .build();

    assertThat(InstallationPhaseUtil.getInstallationPhasesForNominatedInstallationDetail(detail))
        .isEmpty();
  }

  @Test
  void getInstallationPhasesForNominatedInstallationDetail_whenForAllWellPhases_thenAllValues() {
    var detail = NominatedInstallationDetailTestUtil.builder()
        .withForAllInstallationPhases(true)
        .withDevelopmentDesignPhase(null)
        .withDevelopmentConstructionPhase(null)
        .withDevelopmentInstallationPhase(null)
        .withDevelopmentCommissioningPhase(null)
        .withDevelopmentProductionPhase(null)
        .withDecommissioningPhase(null)
        .build();

    assertThat(InstallationPhaseUtil.getInstallationPhasesForNominatedInstallationDetail(detail))
        .containsExactlyInAnyOrder(InstallationPhase.values());
  }

  @Test
  void getInstallationPhasesForNominatedInstallationDetail_whenNotForAllWellPhases_andAllSelected_thenAllValues() {
    var detail = NominatedInstallationDetailTestUtil.builder()
        .withForAllInstallationPhases(false)
        .withDevelopmentDesignPhase(true)
        .withDevelopmentConstructionPhase(true)
        .withDevelopmentInstallationPhase(true)
        .withDevelopmentCommissioningPhase(true)
        .withDevelopmentProductionPhase(true)
        .withDecommissioningPhase(true)
        .build();

    assertThat(InstallationPhaseUtil.getInstallationPhasesForNominatedInstallationDetail(detail))
        .containsExactlyInAnyOrder(InstallationPhase.values());
  }

  @ParameterizedTest
  @EnumSource(InstallationPhase.class)
  void getInstallationPhasesForNominatedInstallationDetail_whenNotForAllWellPhases_andEnumIsSelected_thenValueIsReturned(
      InstallationPhase installationPhase
  ) {
    var detailBuilder = NominatedInstallationDetailTestUtil.builder()
        .withForAllInstallationPhases(false)
        .withDevelopmentDesignPhase(null)
        .withDevelopmentConstructionPhase(null)
        .withDevelopmentInstallationPhase(null)
        .withDevelopmentCommissioningPhase(null)
        .withDevelopmentProductionPhase(null)
        .withDecommissioningPhase(null);

    switch (installationPhase) {
      case DEVELOPMENT_DESIGN -> detailBuilder.withDevelopmentDesignPhase(true);
      case DEVELOPMENT_CONSTRUCTION -> detailBuilder.withDevelopmentConstructionPhase(true);
      case DEVELOPMENT_INSTALLATION -> detailBuilder.withDevelopmentInstallationPhase(true);
      case DEVELOPMENT_COMMISSIONING -> detailBuilder.withDevelopmentCommissioningPhase(true);
      case DEVELOPMENT_PRODUCTION -> detailBuilder.withDevelopmentProductionPhase(true);
      case DECOMMISSIONING -> detailBuilder.withDecommissioningPhase(true);
    }

    var detail = detailBuilder.build();

    assertThat(InstallationPhaseUtil.getInstallationPhasesForNominatedInstallationDetail(detail))
        .containsExactly(installationPhase);
  }
}