package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class WellExclusionFormTestUtil {

  private WellExclusionFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private List<String> excludedWells = new ArrayList<>();

    private String hasWellsToExclude = "false";

    private boolean wellsAdded = false;

    private Builder() {}

    Builder hasWellsToExclude(Boolean hasWellsToExclude) {
      this.hasWellsToExclude = Objects.toString(hasWellsToExclude, null);
      return this;
    }

    Builder hasWellsToExclude(String hasWellsToExclude) {
      this.hasWellsToExclude = hasWellsToExclude;
      return this;
    }

    Builder withExcludedWell(String wellboreId) {
      excludedWells.add(wellboreId);
      wellsAdded = true;
      return this;
    }

    Builder withExcludedWells(List<String> excludedWells) {
      this.excludedWells = excludedWells;
      wellsAdded = true;
      return this;
    }

    WellExclusionForm build() {

      if (!wellsAdded) {
        excludedWells.add("wellbore id");
      }

      var form = new WellExclusionForm();
      form.setHasWellsToExclude(hasWellsToExclude);
      form.setExcludedWells(excludedWells);
      return form;
    }

  }
}