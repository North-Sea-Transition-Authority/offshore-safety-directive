package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Collections;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;

public class NominatedBlockSubareaDetailView {

  private final List<LicenceBlockSubareaDto> licenceBlockSubareas;
  private final Boolean validForFutureWellsInSubarea;
  private final Boolean forAllWellPhases;
  private final List<WellPhase> wellPhases;

  public NominatedBlockSubareaDetailView() {
    this(Collections.emptyList(), null, null, Collections.emptyList());
  }

  public NominatedBlockSubareaDetailView(List<LicenceBlockSubareaDto> licenceBlockSubareas,
                                         Boolean validForFutureWellsInSubarea, Boolean forAllWellPhases,
                                         List<WellPhase> wellPhases) {
    this.licenceBlockSubareas = licenceBlockSubareas;
    this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
    this.forAllWellPhases = forAllWellPhases;
    this.wellPhases = wellPhases;
  }

  public List<LicenceBlockSubareaDto> getLicenceBlockSubareas() {
    return licenceBlockSubareas;
  }

  public Boolean getValidForFutureWellsInSubarea() {
    return validForFutureWellsInSubarea;
  }

  public Boolean getForAllWellPhases() {
    return forAllWellPhases;
  }

  public List<WellPhase> getWellPhases() {
    return wellPhases;
  }
}
