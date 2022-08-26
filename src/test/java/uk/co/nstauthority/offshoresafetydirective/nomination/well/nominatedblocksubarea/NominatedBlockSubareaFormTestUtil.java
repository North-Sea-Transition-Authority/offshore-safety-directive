package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea;

import java.util.List;

public class NominatedBlockSubareaFormTestUtil {

  private NominatedBlockSubareaFormTestUtil() {
    throw new IllegalStateException("NominatedBlockSubareaFormTestUtil is a test util and should not be instantiated");
  }
  
  public static class NominatedBlockSubareaFormBuilder {
    private List<Integer> subareas = List.of(1, 2);

    private Boolean validForFutureWellsInSubarea = true;
    private Boolean forAllWellPhases = false;
    private Boolean explorationAndAppraisalPhase = true;
    private Boolean developmentPhase = true;
    private Boolean decommissioningPhase = true;

    public NominatedBlockSubareaFormBuilder withSubareas(List<Integer> subareas) {
      this.subareas = subareas;
      return this;
    }

    public NominatedBlockSubareaFormBuilder withValidForFutureWellsInSubarea(Boolean validForFutureWellsInSubarea) {
      this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
      return this;
    }

    public NominatedBlockSubareaFormBuilder withForAllWellPhases(Boolean forAllWellPhases) {
      this.forAllWellPhases = forAllWellPhases;
      return this;
    }

    public NominatedBlockSubareaFormBuilder withExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
      this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
      return this;
    }

    public NominatedBlockSubareaFormBuilder withDevelopmentPhase(Boolean developmentPhase) {
      this.developmentPhase = developmentPhase;
      return this;
    }

    public NominatedBlockSubareaFormBuilder withDecommissioningPhase(Boolean decommissioningPhase) {
      this.decommissioningPhase = decommissioningPhase;
      return this;
    }

    public NominatedBlockSubareaForm build() {
      return new NominatedBlockSubareaForm()
          .setSubareas(subareas)
          .setValidForFutureWellsInSubarea(validForFutureWellsInSubarea)
          .setForAllWellPhases(forAllWellPhases)
          .setExplorationAndAppraisalPhase(explorationAndAppraisalPhase)
          .setDevelopmentPhase(developmentPhase)
          .setDecommissioningPhase(decommissioningPhase);
    }
  }
}
