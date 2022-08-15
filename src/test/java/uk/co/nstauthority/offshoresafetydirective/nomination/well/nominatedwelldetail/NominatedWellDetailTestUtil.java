package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class NominatedWellDetailTestUtil {

  private static final int wellId1 = 16;
  private static final int wellId2 = 3;
  private static final int wellId3 = 99;

  private NominatedWellDetailTestUtil() {
    throw new IllegalStateException("NominatedWellDetailTestUtil is a util class and should not be instantiated");
  }

  public static NominatedWellDetailForm getValidForm() {
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases(false);
    form.setExplorationAndAppraisalPhase(true);
    form.setDevelopmentPhase(true);
    form.setDecommissioningPhase(true);
    form.setWells(List.of(wellId1, wellId2, wellId3));
    return form;
  }

  public static class NominatedWellDetailBuilder {
    private NominationDetail nominationDetail = NominationDetailTestUtil.getNominationDetail();
    private Boolean forAllWellPhases = true;
    private Boolean explorationAndAppraisalPhase;
    private Boolean developmentPhase;
    private Boolean decommissioningPhase;

    public NominatedWellDetailBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public NominatedWellDetailBuilder withForAllWellPhases(Boolean forAllWellPhases) {
      this.forAllWellPhases = forAllWellPhases;
      return this;
    }

    public NominatedWellDetailBuilder withExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
      this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
      return this;
    }

    public NominatedWellDetailBuilder withDevelopmentPhase(Boolean developmentPhase) {
      this.developmentPhase = developmentPhase;
      return this;
    }

    public NominatedWellDetailBuilder withDecommissioningPhase(Boolean decommissioningPhase) {
      this.decommissioningPhase = decommissioningPhase;
      return this;
    }

    public NominatedWellDetail build() {
      return new NominatedWellDetail()
          .setNominationDetail(nominationDetail)
          .setForAllWellPhases(forAllWellPhases)
          .setExplorationAndAppraisalPhase(explorationAndAppraisalPhase)
          .setDevelopmentPhase(developmentPhase)
          .setDecommissioningPhase(decommissioningPhase);
    }
  }
}
