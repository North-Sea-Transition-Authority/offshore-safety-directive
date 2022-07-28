package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

public class NominatedWellDetailTestUtil {

  private static final int wellId1 = 16;
  private static final int wellId2 = 3;
  private static final int wellId3 = 99;

  private NominatedWellDetailTestUtil() {
    throw new IllegalStateException("NominatedWellDetailTestUtil is a util class and should not be instantiated");
  }

  static NominatedWellDetail getNominatedWellDetail(NominationDetail nominationDetail) {
    var nominatedWellDetail = new NominatedWellDetail(nominationDetail, false);
    nominatedWellDetail.setExplorationAndAppraisalPhase(true);
    nominatedWellDetail.setDevelopmentPhase(true);
    nominatedWellDetail.setDecommissioningPhase(true);
    return nominatedWellDetail;
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
}
