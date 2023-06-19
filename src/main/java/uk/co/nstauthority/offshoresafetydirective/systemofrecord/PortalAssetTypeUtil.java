package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;

public class PortalAssetTypeUtil {

  private PortalAssetTypeUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Class<? extends DisplayableEnumOption> getEnumPhaseClass(PortalAssetType portalAssetType) {
    return switch (portalAssetType) {
      case INSTALLATION -> InstallationPhase.class;
      case SUBAREA, WELLBORE -> WellPhase.class;
    };
  }

}
