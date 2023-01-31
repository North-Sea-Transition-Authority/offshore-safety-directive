package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;

class NominatedBlockSubareaFormTestUtil {

  private NominatedBlockSubareaFormTestUtil() {
    throw new IllegalStateException("NominatedBlockSubareaFormTestUtil is a test util and should not be instantiated");
  }

  static NominatedBlockSubareaFormBuilder builder() {
    return new NominatedBlockSubareaFormBuilder();
  }
  
  static class NominatedBlockSubareaFormBuilder {
    private List<String> subareas = List.of("1", "2");

    private Boolean validForFutureWellsInSubarea = true;
    private Boolean forAllWellPhases = false;
    private Boolean explorationAndAppraisalPhase = true;
    private Boolean developmentPhase = true;
    private Boolean decommissioningPhase = true;

    NominatedBlockSubareaFormBuilder withSubareas(List<String> subareas) {
      this.subareas = subareas;
      return this;
    }

    NominatedBlockSubareaFormBuilder withValidForFutureWellsInSubarea(Boolean validForFutureWellsInSubarea) {
      this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
      return this;
    }

    NominatedBlockSubareaFormBuilder withForAllWellPhases(Boolean forAllWellPhases) {
      this.forAllWellPhases = forAllWellPhases;
      return this;
    }

    NominatedBlockSubareaFormBuilder withExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
      this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
      return this;
    }

    NominatedBlockSubareaFormBuilder withDevelopmentPhase(Boolean developmentPhase) {
      this.developmentPhase = developmentPhase;
      return this;
    }

    NominatedBlockSubareaFormBuilder withDecommissioningPhase(Boolean decommissioningPhase) {
      this.decommissioningPhase = decommissioningPhase;
      return this;
    }

    NominatedBlockSubareaForm build() {
      var form = new NominatedBlockSubareaForm();
      form.setSubareas(subareas);
      form.setValidForFutureWellsInSubarea(validForFutureWellsInSubarea);
      form.setForAllWellPhases(forAllWellPhases);
      form.setExplorationAndAppraisalPhase(explorationAndAppraisalPhase);
      form.setDevelopmentPhase(developmentPhase);
      form.setDecommissioningPhase(decommissioningPhase);
      return form;
    }
  }
}
