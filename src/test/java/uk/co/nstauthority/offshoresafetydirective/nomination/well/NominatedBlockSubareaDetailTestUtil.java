package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class NominatedBlockSubareaDetailTestUtil {

  private NominatedBlockSubareaDetailTestUtil() {
    throw new IllegalStateException("NominatedBlockSubareaDetailTestUtil is a test util and should not be instantiated");
  }

  static NominatedBlockSubareaDetailBuilder builder() {
    return new NominatedBlockSubareaDetailBuilder();
  }

  static class NominatedBlockSubareaDetailBuilder {
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Boolean validForFutureWellsInSubarea = true;
    private Boolean forAllWellPhases = false;
    private Boolean explorationAndAppraisalPhase = true;
    private Boolean developmentPhase = true;
    private Boolean decommissioningPhase = true;

    NominatedBlockSubareaDetailBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    NominatedBlockSubareaDetailBuilder withValidForFutureWellsInSubarea(Boolean validForFutureWellsInSubarea) {
      this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
      return this;
    }

    NominatedBlockSubareaDetailBuilder withForAllWellPhases(Boolean forAllWellPhases) {
      this.forAllWellPhases = forAllWellPhases;
      return this;
    }

    NominatedBlockSubareaDetailBuilder withExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
      this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
      return this;
    }

    NominatedBlockSubareaDetailBuilder withDevelopmentPhase(Boolean developmentPhase) {
      this.developmentPhase = developmentPhase;
      return this;
    }

    NominatedBlockSubareaDetailBuilder withDecommissioningPhase(Boolean decommissioningPhase) {
      this.decommissioningPhase = decommissioningPhase;
      return this;
    }

    NominatedBlockSubareaDetail build() {
      return new NominatedBlockSubareaDetail()
          .setNominationDetail(nominationDetail)
          .setValidForFutureWellsInSubarea(validForFutureWellsInSubarea)
          .setForAllWellPhases(forAllWellPhases)
          .setExplorationAndAppraisalPhase(explorationAndAppraisalPhase)
          .setDevelopmentPhase(developmentPhase)
          .setDecommissioningPhase(decommissioningPhase);
    }
  }
}
