package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import java.util.Objects;

class NominatedBlockSubareaFormTestUtil {

  private NominatedBlockSubareaFormTestUtil() {
    throw new IllegalStateException("NominatedBlockSubareaFormTestUtil is a test util and should not be instantiated");
  }

  static NominatedBlockSubareaFormBuilder builder() {
    return new NominatedBlockSubareaFormBuilder();
  }
  
  static class NominatedBlockSubareaFormBuilder {
    private List<String> subareas = List.of("1", "2");

    private String validForFutureWellsInSubarea = "true";
    private String forAllWellPhases = "false";
    private String explorationAndAppraisalPhase = "true";
    private String developmentPhase = "true";
    private String decommissioningPhase = "false";

    NominatedBlockSubareaFormBuilder withSubareas(List<String> subareas) {
      this.subareas = subareas;
      return this;
    }

    NominatedBlockSubareaFormBuilder withValidForFutureWellsInSubarea(Boolean validForFutureWellsInSubarea) {
      this.validForFutureWellsInSubarea = String.valueOf(validForFutureWellsInSubarea);
      return this;
    }

    NominatedBlockSubareaFormBuilder withValidForFutureWellsInSubarea(String validForFutureWellsInSubarea) {
      this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
      return this;
    }

    NominatedBlockSubareaFormBuilder withForAllWellPhases(Boolean forAllWellPhases) {
      this.forAllWellPhases = String.valueOf(forAllWellPhases);
      return this;
    }

    NominatedBlockSubareaFormBuilder withForAllWellPhases(String forAllWellPhases) {
      this.forAllWellPhases = forAllWellPhases;
      return this;
    }

    NominatedBlockSubareaFormBuilder withExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
      this.explorationAndAppraisalPhase = String.valueOf(explorationAndAppraisalPhase);
      return this;
    }

    NominatedBlockSubareaFormBuilder withExplorationAndAppraisalPhase(String explorationAndAppraisalPhase) {
      this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
      return this;
    }

    NominatedBlockSubareaFormBuilder withDevelopmentPhase(Boolean developmentPhase) {
      this.developmentPhase = String.valueOf(developmentPhase);
      return this;
    }

    NominatedBlockSubareaFormBuilder withDevelopmentPhase(String developmentPhase) {
      this.developmentPhase = developmentPhase;
      return this;
    }

    NominatedBlockSubareaFormBuilder withDecommissioningPhase(Boolean decommissioningPhase) {
      this.decommissioningPhase = String.valueOf(decommissioningPhase);
      return this;
    }

    NominatedBlockSubareaFormBuilder withDecommissioningPhase(String decommissioningPhase) {
      this.decommissioningPhase = decommissioningPhase;
      return this;
    }

    NominatedBlockSubareaForm build() {
      var form = new NominatedBlockSubareaForm();
      form.setSubareas(subareas);
      form.setValidForFutureWellsInSubarea(Objects.toString(validForFutureWellsInSubarea, null));
      form.setForAllWellPhases(Objects.toString(forAllWellPhases, null));
      form.setExplorationAndAppraisalPhase(Objects.toString(explorationAndAppraisalPhase, null));
      form.setDevelopmentPhase(Objects.toString(developmentPhase, null));
      form.setDecommissioningPhase(Objects.toString(decommissioningPhase, null));
      return form;
    }
  }
}
