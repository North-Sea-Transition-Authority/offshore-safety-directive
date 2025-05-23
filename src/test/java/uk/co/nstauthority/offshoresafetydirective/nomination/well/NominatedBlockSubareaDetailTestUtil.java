package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.UUID;
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
    private UUID id = UUID.randomUUID();
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Boolean validForFutureWellsInSubarea = true;
    private Boolean forAllWellPhases = false;
    private Boolean explorationAndAppraisalPhase = true;
    private Boolean developmentPhase = true;
    private Boolean decommissioningPhase = true;

    NominatedBlockSubareaDetailBuilder withId(UUID id) {
      this.id = id;
      return this;
    }

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
      var subareaDetail = new NominatedBlockSubareaDetail(id);
      subareaDetail.setNominationDetail(nominationDetail);
      subareaDetail.setValidForFutureWellsInSubarea(validForFutureWellsInSubarea);
      subareaDetail.setForAllWellPhases(forAllWellPhases);
      subareaDetail.setExplorationAndAppraisalPhase(explorationAndAppraisalPhase);
      subareaDetail.setDevelopmentPhase(developmentPhase);
      subareaDetail.setDecommissioningPhase(decommissioningPhase);
      return subareaDetail;
    }
  }
}
