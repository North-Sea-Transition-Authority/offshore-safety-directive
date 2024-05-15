package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.Collections;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreRegistrationNumber;

public record ExcludedWellView(
    Boolean hasWellsToExclude,
    List<WellboreRegistrationNumber> excludedWells
) {

  public ExcludedWellView() {
    this(null, Collections.emptyList());
  }
}
