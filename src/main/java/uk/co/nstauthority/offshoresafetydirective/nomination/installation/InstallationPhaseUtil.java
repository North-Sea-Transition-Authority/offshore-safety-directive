package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class InstallationPhaseUtil {

  private InstallationPhaseUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Set<InstallationPhase> getInstallationPhasesForNominatedInstallationDetail(
      NominatedInstallationDetail detail
  ) {
    if (BooleanUtils.isTrue(detail.getForAllInstallationPhases())) {
      return Arrays.stream(InstallationPhase.values())
          .collect(Collectors.toSet());
    }
    return Arrays.stream(InstallationPhase.values())
        .filter(installationPhase ->
          switch (installationPhase) {
            case DECOMMISSIONING -> BooleanUtils.isTrue(detail.getDecommissioningPhase());
            case DEVELOPMENT_DESIGN -> BooleanUtils.isTrue(detail.getDevelopmentDesignPhase());
            case DEVELOPMENT_COMMISSIONING -> BooleanUtils.isTrue(detail.getDevelopmentCommissioningPhase());
            case DEVELOPMENT_CONSTRUCTION -> BooleanUtils.isTrue(detail.getDevelopmentConstructionPhase());
            case DEVELOPMENT_INSTALLATION -> BooleanUtils.isTrue(detail.getDevelopmentInstallationPhase());
            case DEVELOPMENT_PRODUCTION -> BooleanUtils.isTrue(detail.getDevelopmentProductionPhase());
          }
        )
        .collect(Collectors.toSet());
  }

}
