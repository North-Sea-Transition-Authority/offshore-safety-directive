package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.ArrayList;
import java.util.List;

public class WellExclusionForm {

  private List<String> excludedWells = new ArrayList<>();

  private Boolean hasWellsToExclude;

  public Boolean hasWellsToExclude() {
    return hasWellsToExclude;
  }

  public void setHasWellsToExclude(Boolean hasWellsToExclude) {
    this.hasWellsToExclude = hasWellsToExclude;
  }

  public Boolean getHasWellsToExclude() {
    return hasWellsToExclude;
  }

  public List<String> getExcludedWells() {
    return excludedWells;
  }

  public void setExcludedWells(List<String> excludedWells) {
    this.excludedWells = excludedWells;
  }
}
