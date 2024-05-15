package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Collections;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryItemView;

public class NominatedWellDetailView {

  private final List<WellSummaryItemView> wells;
  private final Boolean isNominationForAllWellPhases;
  private final List<WellPhase> wellPhases;

  public NominatedWellDetailView() {
    this(Collections.emptyList(), null, Collections.emptyList());
  }

  public NominatedWellDetailView(List<WellSummaryItemView> wells, Boolean isNominationForAllWellPhases,
                                 List<WellPhase> wellPhases) {
    this.wells = wells;
    this.isNominationForAllWellPhases = isNominationForAllWellPhases;
    this.wellPhases = wellPhases;
  }

  public List<WellSummaryItemView> getWells() {
    return wells;
  }

  public Boolean getIsNominationForAllWellPhases() {
    return isNominationForAllWellPhases;
  }

  public List<WellPhase> getWellPhases() {
    return wellPhases;
  }
}