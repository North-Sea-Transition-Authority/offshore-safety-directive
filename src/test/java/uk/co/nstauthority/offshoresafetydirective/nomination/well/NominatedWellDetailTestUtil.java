package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class NominatedWellDetailTestUtil {

  private NominatedWellDetailTestUtil() {
    throw new IllegalStateException("NominatedWellDetailTestUtil is a util class and should not be instantiated");
  }

  public static NominatedWellDetailBuilder builder() {
    return new NominatedWellDetailBuilder();
  }

  static class NominatedWellDetailBuilder {
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Boolean forAllWellPhases = true;
    private Boolean explorationAndAppraisalPhase;
    private Boolean developmentPhase;
    private Boolean decommissioningPhase;

    NominatedWellDetailBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    NominatedWellDetailBuilder withForAllWellPhases(Boolean forAllWellPhases) {
      this.forAllWellPhases = forAllWellPhases;
      return this;
    }

    NominatedWellDetailBuilder withExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
      this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
      return this;
    }

    NominatedWellDetailBuilder withDevelopmentPhase(Boolean developmentPhase) {
      this.developmentPhase = developmentPhase;
      return this;
    }

    NominatedWellDetailBuilder withDecommissioningPhase(Boolean decommissioningPhase) {
      this.decommissioningPhase = decommissioningPhase;
      return this;
    }

    NominatedWellDetail build() {
      return new NominatedWellDetail()
          .setNominationDetail(nominationDetail)
          .setForAllWellPhases(forAllWellPhases)
          .setExplorationAndAppraisalPhase(explorationAndAppraisalPhase)
          .setDevelopmentPhase(developmentPhase)
          .setDecommissioningPhase(decommissioningPhase);
    }
  }
}
