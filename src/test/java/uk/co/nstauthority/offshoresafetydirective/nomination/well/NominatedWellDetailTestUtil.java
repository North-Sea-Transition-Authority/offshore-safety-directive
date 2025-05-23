package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.UUID;
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
    private UUID id = UUID.randomUUID();
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Boolean forAllWellPhases = true;
    private Boolean explorationAndAppraisalPhase;
    private Boolean developmentPhase;
    private Boolean decommissioningPhase;

    NominatedWellDetailBuilder withId(UUID id) {
      this.id = id;
      return this;
    }

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
      var wellDetail = new NominatedWellDetail(id);
      wellDetail.setNominationDetail(nominationDetail);
      wellDetail.setForAllWellPhases(forAllWellPhases);
      wellDetail.setExplorationAndAppraisalPhase(explorationAndAppraisalPhase);
      wellDetail.setDevelopmentPhase(developmentPhase);
      wellDetail.setDecommissioningPhase(decommissioningPhase);
      return wellDetail;
    }
  }
}
