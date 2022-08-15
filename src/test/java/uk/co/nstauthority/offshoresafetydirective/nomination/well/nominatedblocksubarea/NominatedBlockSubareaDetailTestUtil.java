package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class NominatedBlockSubareaDetailTestUtil {

  private NominatedBlockSubareaDetailTestUtil() {
    throw new IllegalStateException("NominatedBlockSubareaDetailTestUtil is a test util and should not be instantiated");
  }

  public static class NominatedBlockSubareaDetailBuilder {
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Boolean validForFutureWellsInSubarea = true;
    private Boolean explorationAndAppraisalPhase = true;
    private Boolean developmentPhase = true;
    private Boolean decommissioningPhase = true;

    public NominatedBlockSubareaDetailBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public NominatedBlockSubareaDetailBuilder withValidForFutureWellsInSubarea(Boolean validForFutureWellsInSubarea) {
      this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
      return this;
    }

    public NominatedBlockSubareaDetailBuilder withExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
      this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
      return this;
    }

    public NominatedBlockSubareaDetailBuilder withDevelopmentPhase(Boolean developmentPhase) {
      this.developmentPhase = developmentPhase;
      return this;
    }

    public NominatedBlockSubareaDetailBuilder withDecommissioningPhase(Boolean decommissioningPhase) {
      this.decommissioningPhase = decommissioningPhase;
      return this;
    }

    public NominatedBlockSubareaDetail build() {
      return new NominatedBlockSubareaDetail()
          .setNominationDetail(nominationDetail)
          .setValidForFutureWellsInSubarea(validForFutureWellsInSubarea)
          .setExplorationAndAppraisalPhase(explorationAndAppraisalPhase)
          .setDevelopmentPhase(developmentPhase)
          .setDecommissioningPhase(decommissioningPhase);
    }
  }
}
