package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominatedBlockSubareaDetailViewTestUtil {

  private NominatedBlockSubareaDetailViewTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private List<LicenceBlockSubareaDto> licenceBlockSubareas = new ArrayList<>();
    private Boolean validForFutureWellsInSubarea = true;
    private Boolean forAllWellPhases = false;
    private List<WellPhase> wellPhases = new ArrayList<>();

    private Builder() {
    }

    public Builder withLicenceBlockSubareas(List<LicenceBlockSubareaDto> licenceBlockSubareas) {
      this.licenceBlockSubareas = licenceBlockSubareas;
      return this;
    }

    public Builder addLicenceBlockSubarea(LicenceBlockSubareaDto licenceBlockSubareaDto) {
      this.licenceBlockSubareas.add(licenceBlockSubareaDto);
      return this;
    }

    public Builder withValidForFutureWellsInSubarea(Boolean validForFutureWellsInSubarea) {
      this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
      return this;
    }

    public Builder withForAllWellPhases(Boolean forAllWellPhases) {
      this.forAllWellPhases = forAllWellPhases;
      return this;
    }

    public Builder withWellPhases(List<WellPhase> wellPhases) {
      this.wellPhases = wellPhases;
      return this;
    }

    public Builder addWellPhase(WellPhase wellPhase) {
      this.wellPhases.add(wellPhase);
      return this;
    }

    public NominatedBlockSubareaDetailView build() {
      return new NominatedBlockSubareaDetailView(
          licenceBlockSubareas,
          validForFutureWellsInSubarea,
          forAllWellPhases,
          wellPhases
      );
    }
  }
}