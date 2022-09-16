package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;

public class NominatedWellDetailViewTestUtil {

  private NominatedWellDetailViewTestUtil() {
    throw new IllegalStateException("NominatedWellDetailViewTestUtil is a util class and should not be instantiated");
  }

  public static class NominatedWellDetailViewBuilder {
    private List<WellDto> wellDtos = List.of(new WellDto(1, "well", "0001"));
    private Boolean isNominationForALlWellPhases = false;
    private List<WellPhase> wellPhases = List.of(WellPhase.DEVELOPMENT, WellPhase.DECOMMISSIONING);

    public NominatedWellDetailViewBuilder withWellDtos(List<WellDto> wellDtos) {
      this.wellDtos = wellDtos;
      return this;
    }

    public NominatedWellDetailViewBuilder withIsNominationForALlWellPhases(Boolean isNominationForALlWellPhases) {
      this.isNominationForALlWellPhases = isNominationForALlWellPhases;
      return this;
    }

    public NominatedWellDetailViewBuilder withWellPhases(List<WellPhase> wellPhases) {
      this.wellPhases = wellPhases;
      return this;
    }

    public NominatedWellDetailView build() {
      return new NominatedWellDetailView(wellDtos, isNominationForALlWellPhases, wellPhases);
    }
  }
}
