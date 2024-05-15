package uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells;

import java.util.Collections;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryItemView;

public record NominatedSubareaWellsView(List<WellSummaryItemView> nominatedSubareaWellbores) {

  public NominatedSubareaWellsView() {
    this(Collections.emptyList());
  }
}
