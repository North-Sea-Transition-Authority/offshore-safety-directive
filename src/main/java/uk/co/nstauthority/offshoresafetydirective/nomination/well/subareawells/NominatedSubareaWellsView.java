package uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells;

import java.util.Collections;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;

public record NominatedSubareaWellsView(List<WellDto> nominatedSubareaWellbores) {

  public NominatedSubareaWellsView() {
    this(Collections.emptyList());
  }
}
