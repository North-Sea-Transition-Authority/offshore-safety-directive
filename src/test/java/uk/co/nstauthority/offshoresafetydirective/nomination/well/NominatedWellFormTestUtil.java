package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class NominatedWellFormTestUtil {

  private NominatedWellFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Boolean isForAllWellPhases = true;
    private Boolean isExplorationAndAppraisalPhase = false;
    private Boolean isDevelopmentPhase = false;
    private Boolean isDecommissioningPhase = false;
    private List<Integer> wells = new ArrayList<>();

    Builder isForAllWellPhases(Boolean isForAllWellPhases) {
      this.isForAllWellPhases = isForAllWellPhases;
      return this;
    }

    Builder isExplorationAndAppraisalPhase(Boolean isExplorationAndAppraisalPhase) {
      this.isExplorationAndAppraisalPhase = isExplorationAndAppraisalPhase;
      return this;
    }

    Builder isDevelopmentPhase(Boolean isDevelopmentPhase) {
      this.isDevelopmentPhase = isDevelopmentPhase;
      return this;
    }

    Builder isDecommissioningPhase(Boolean isDecommissioningPhase) {
      this.isDecommissioningPhase = isDecommissioningPhase;
      return this;
    }

    Builder withWell(int wellId) {
      wells.add(wellId);
      return this;
    }

    Builder withWells(List<Integer> wellIds) {
      wells = wellIds;
      return this;
    }

    NominatedWellDetailForm build() {
      var form = new NominatedWellDetailForm();
      form.setForAllWellPhases(isForAllWellPhases);
      form.setExplorationAndAppraisalPhase(isExplorationAndAppraisalPhase);
      form.setDevelopmentPhase(isDevelopmentPhase);
      form.setDecommissioningPhase(isDecommissioningPhase);

      if (wells.isEmpty()) {
        wells.add(10);
        wells.add(20);
      }

      form.setWells(wells);

      return form;
    }

  }
}
