package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import java.util.Arrays;
import java.util.Objects;
import uk.co.fivium.energyportalapi.generated.types.WellboreIntent;

public enum WonsWellboreIntent {

  EXPLORATION(WellboreIntent.EXPLORATION),
  APPRAISAL(WellboreIntent.APPRAISAL),
  DEVELOPMENT(WellboreIntent.DEVELOPMENT),
  CARBON_CAPTURE_AND_STORAGE(WellboreIntent.CARBON_CAPTURE_AND_STORAGE);

  private final WellboreIntent portalWellboreIntent;

  WonsWellboreIntent(WellboreIntent portalWellboreIntent) {
    this.portalWellboreIntent = portalWellboreIntent;
  }

  public WellboreIntent getPortalWellboreIntent() {
    return portalWellboreIntent;
  }

  static WonsWellboreIntent fromPortalIntent(WellboreIntent portalIntent) {
    return Arrays.stream(WonsWellboreIntent.values())
        .filter(intent -> Objects.equals(intent.getPortalWellboreIntent(), portalIntent))
        .findFirst()
        .orElse(null);
  }
}
