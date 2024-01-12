package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryItemView;

public class NominatedWellDetailViewTestUtil {

  private NominatedWellDetailViewTestUtil() {
    throw new IllegalStateException("NominatedWellDetailViewTestUtil is a util class and should not be instantiated");
  }

  public static NominatedWellDetailViewBuilder builder() {
    return new NominatedWellDetailViewBuilder();
  }

  public static class NominatedWellDetailViewBuilder {
    private List<WellSummaryItemView> wellSummaryItemViews = new ArrayList<>();
    private Boolean isNominationForALlWellPhases = false;
    private List<WellPhase> wellPhases = new ArrayList<>();

    private boolean wellsAdded = false;

    private boolean wellPhasesAdded = false;

    private NominatedWellDetailViewBuilder() {}

    public NominatedWellDetailViewBuilder withWellSummaryItemViews(List<WellSummaryItemView> wellDtos) {
      this.wellSummaryItemViews = wellDtos;
      this.wellsAdded = true;
      return this;
    }

    public NominatedWellDetailViewBuilder withWellSummaryItemView(WellSummaryItemView wellDto) {
      this.wellSummaryItemViews.add(wellDto);
      this.wellsAdded = true;
      return this;
    }

    public NominatedWellDetailViewBuilder withIsNominationForAllWellPhases(Boolean isNominationForALlWellPhases) {
      this.isNominationForALlWellPhases = isNominationForALlWellPhases;
      return this;
    }

    public NominatedWellDetailViewBuilder withWellPhases(List<WellPhase> wellPhases) {
      this.wellPhases = wellPhases;
      this.wellPhasesAdded = true;
      return this;
    }

    public NominatedWellDetailViewBuilder withWellPhase(WellPhase wellPhase) {
      this.wellPhases.add(wellPhase);
      this.wellPhasesAdded = true;
      return this;
    }

    public NominatedWellDetailView build() {

      if (!wellsAdded) {
        var wellDto = WellDtoTestUtil.builder().build();
        wellSummaryItemViews.add(WellSummaryItemView.fromWellDto(wellDto));
      }

      if (!wellPhasesAdded) {
        wellPhases.addAll(List.of(WellPhase.DEVELOPMENT, WellPhase.DECOMMISSIONING));
      }

      return new NominatedWellDetailView(wellSummaryItemViews, isNominationForALlWellPhases, wellPhases);
    }
  }
}
