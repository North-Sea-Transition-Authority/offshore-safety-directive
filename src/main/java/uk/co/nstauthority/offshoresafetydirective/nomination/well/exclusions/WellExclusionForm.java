package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.ArrayList;
import java.util.List;

public class WellExclusionForm {

  private List<String> excludedWells = new ArrayList<>();

  private String hasWellsToExclude;

  public String hasWellsToExclude() {
    return hasWellsToExclude;
  }

  public String getHasWellsToExclude() {
    return hasWellsToExclude;
  }

  public void setHasWellsToExclude(String hasWellsToExclude) {
    this.hasWellsToExclude = hasWellsToExclude;
  }

  public List<String> getExcludedWells() {
    return excludedWells;
  }

  public void setExcludedWells(List<String> excludedWells) {
    this.excludedWells = excludedWells;
  }
}
