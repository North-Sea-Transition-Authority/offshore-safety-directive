package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;

public class NominatedWellDetailViewTestUtil {

  private NominatedWellDetailViewTestUtil() {
    throw new IllegalStateException("NominatedWellDetailViewTestUtil is a util class and should not be instantiated");
  }

  public static NominatedWellDetailViewBuilder builder() {
    return new NominatedWellDetailViewBuilder();
  }

  public static class NominatedWellDetailViewBuilder {
    private List<WellDto> wellDtos = new ArrayList<>();
    private Boolean isNominationForALlWellPhases = false;
    private List<WellPhase> wellPhases = new ArrayList<>();

    private NominatedWellDetailViewBuilder() {
      wellDtos.add(WellDtoTestUtil.builder().build());
      wellPhases.addAll(List.of(WellPhase.DEVELOPMENT, WellPhase.DECOMMISSIONING));
    }

    public NominatedWellDetailViewBuilder withWellDtos(List<WellDto> wellDtos) {
      this.wellDtos = wellDtos;
      return this;
    }

    public NominatedWellDetailViewBuilder withWellDto(WellDto wellDto) {
      this.wellDtos.add(wellDto);
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

    public NominatedWellDetailViewBuilder withWellPhase(WellPhase wellPhase) {
      this.wellPhases.add(wellPhase);
      return this;
    }

    public NominatedWellDetailView build() {
      return new NominatedWellDetailView(wellDtos, isNominationForALlWellPhases, wellPhases);
    }
  }
}
