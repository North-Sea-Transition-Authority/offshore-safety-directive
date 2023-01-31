package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import uk.co.fivium.energyportalapi.generated.types.Wellbore;

public record WellDto(WellboreId wellboreId, String name) {

  static WellDto fromPortalWellbore(Wellbore wellbore) {
    return new WellDto(
        new WellboreId(wellbore.getId()),
        wellbore.getRegistrationNumber()
    );
  }
}
