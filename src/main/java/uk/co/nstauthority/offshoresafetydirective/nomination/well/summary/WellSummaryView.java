package uk.co.nstauthority.offshoresafetydirective.nomination.well.summary;

import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionDetails;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionId;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionName;

public class WellSummaryView {

  private static final String SUMMARY_ID = "well-summary";
  private static final String SUMMARY_NAME = "Wells";

  private final WellSelectionType wellSelectionType;

  private final NominatedWellDetailView specificWellSummaryView;

  private final NominatedBlockSubareaDetailView subareaWellSummaryView;

  private final ExcludedWellView excludedWellSummaryView;

  private final List<WellSummaryItemView> subareaWellsIncludedOnNomination;

  private final SummarySectionError summarySectionError;

  private WellSummaryView(WellSelectionType wellSelectionType,
                          NominatedWellDetailView specificWellSummaryView,
                          NominatedBlockSubareaDetailView subareaWellSummaryView,
                          ExcludedWellView excludedWellSummaryView,
                          List<WellSummaryItemView> subareaWellsIncludedOnNomination,
                          SummarySectionError summarySectionError) {
    this.wellSelectionType = wellSelectionType;
    this.specificWellSummaryView = specificWellSummaryView;
    this.subareaWellSummaryView = subareaWellSummaryView;
    this.excludedWellSummaryView = excludedWellSummaryView;
    this.subareaWellsIncludedOnNomination = subareaWellsIncludedOnNomination;
    this.summarySectionError = summarySectionError;
  }

  public WellSelectionType getWellSelectionType() {
    return wellSelectionType;
  }

  public NominatedWellDetailView getSpecificWellSummaryView() {
    return specificWellSummaryView;
  }

  public NominatedBlockSubareaDetailView getSubareaWellSummaryView() {
    return subareaWellSummaryView;
  }

  public ExcludedWellView getExcludedWellSummaryView() {
    return excludedWellSummaryView;
  }

  public List<WellSummaryItemView> getSubareaWellsIncludedOnNomination() {
    return subareaWellsIncludedOnNomination;
  }

  public SummarySectionError getSummarySectionError() {
    return summarySectionError;
  }

  public SummarySectionDetails getSummarySectionDetail() {
    return new SummarySectionDetails(
        new SummarySectionId(SUMMARY_ID),
        new SummarySectionName(SUMMARY_NAME)
    );
  }

  public static Builder builder(WellSelectionType wellSelectionType) {
    return new Builder(wellSelectionType);
  }

  public static class Builder {

    private final WellSelectionType wellSelectionType;

    private NominatedWellDetailView specificWellSummaryView;

    private NominatedBlockSubareaDetailView subareaWellSummaryView;

    private ExcludedWellView excludedWellSummaryView;

    private List<WellSummaryItemView> subareaWellsIncludedOnNomination;

    private SummarySectionError summarySectionError;

    private Builder(WellSelectionType wellSelectionType) {
      this.wellSelectionType = wellSelectionType;
    }

    public Builder withSpecificWellSummaryView(NominatedWellDetailView specificWellSummaryView) {
      this.specificWellSummaryView = specificWellSummaryView;
      return this;
    }

    public Builder withSubareaSummary(NominatedBlockSubareaDetailView subareaWellSummaryView) {
      this.subareaWellSummaryView = subareaWellSummaryView;
      return this;
    }

    public Builder withExcludedWellSummaryView(ExcludedWellView excludedWellSummaryView) {
      this.excludedWellSummaryView = excludedWellSummaryView;
      return this;
    }

    public Builder withSubareaWells(List<WellSummaryItemView> subareaWellsIncludedOnNomination) {
      this.subareaWellsIncludedOnNomination = subareaWellsIncludedOnNomination;
      return this;
    }

    public Builder withSummarySectionError(SummarySectionError summarySectionError) {
      this.summarySectionError = summarySectionError;
      return this;
    }

    public WellSummaryView build() {
      return new WellSummaryView(
          wellSelectionType,
          specificWellSummaryView,
          subareaWellSummaryView,
          excludedWellSummaryView,
          subareaWellsIncludedOnNomination,
          summarySectionError
      );
    }

  }


}
