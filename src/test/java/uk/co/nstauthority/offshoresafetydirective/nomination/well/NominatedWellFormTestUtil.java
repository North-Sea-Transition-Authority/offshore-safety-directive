package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class NominatedWellFormTestUtil {

  private NominatedWellFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String isForAllWellPhases = "true";
    private String isExplorationAndAppraisalPhase = "false";
    private String isDevelopmentPhase = "false";
    private String isDecommissioningPhase = "false";
    private List<String> wells = new ArrayList<>();

    Builder isForAllWellPhases(Boolean isForAllWellPhases) {
      this.isForAllWellPhases = String.valueOf(isForAllWellPhases);
      return this;
    }

    Builder isForAllWellPhases(String isForAllWellPhases) {
      this.isForAllWellPhases = isForAllWellPhases;
      return this;
    }

    Builder isExplorationAndAppraisalPhase(Boolean isExplorationAndAppraisalPhase) {
      this.isExplorationAndAppraisalPhase = String.valueOf(isExplorationAndAppraisalPhase);
      return this;
    }

    Builder isExplorationAndAppraisalPhase(String isExplorationAndAppraisalPhase) {
      this.isExplorationAndAppraisalPhase = isExplorationAndAppraisalPhase;
      return this;
    }

    Builder isDevelopmentPhase(Boolean isDevelopmentPhase) {
      this.isDevelopmentPhase = String.valueOf(isDevelopmentPhase);
      return this;
    }

    Builder isDevelopmentPhase(String isDevelopmentPhase) {
      this.isDevelopmentPhase = isDevelopmentPhase;
      return this;
    }

    Builder isDecommissioningPhase(Boolean isDecommissioningPhase) {
      this.isDecommissioningPhase = String.valueOf(isDecommissioningPhase);
      return this;
    }

    Builder isDecommissioningPhase(String isDecommissioningPhase) {
      this.isDecommissioningPhase = isDecommissioningPhase;
      return this;
    }

    Builder withWell(int wellId) {
      wells.add(Objects.toString(wellId, null));
      return this;
    }

    Builder withWells(List<String> wellIds) {
      wells = wellIds;
      return this;
    }

    NominatedWellDetailForm build() {
      var form = new NominatedWellDetailForm();
      form.setForAllWellPhases(Objects.toString(isForAllWellPhases, null));
      form.setExplorationAndAppraisalPhase(Objects.toString(isExplorationAndAppraisalPhase, null));
      form.setDevelopmentPhase(Objects.toString(isDevelopmentPhase, null));
      form.setDecommissioningPhase(Objects.toString(isDecommissioningPhase, null));

      form.setWells(wells);

      return form;
    }

  }
}
