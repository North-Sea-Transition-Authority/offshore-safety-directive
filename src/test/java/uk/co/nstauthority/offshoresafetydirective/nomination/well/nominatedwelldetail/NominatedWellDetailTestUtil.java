package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

class NominatedWellDetailTestUtil {

  private NominatedWellDetailTestUtil() {
    throw new IllegalStateException("NominatedWellDetailTestUtil is a util class and should not be instantiated");
  }

  static NominatedWellDetail getSpecificWellSetup(NominationDetail nominationDetail) {
    var nominatedWellDetail = new NominatedWellDetail(nominationDetail, false);
    nominatedWellDetail.setExplorationAndAppraisalPhase(true);
    nominatedWellDetail.setDevelopmentPhase(true);
    nominatedWellDetail.setDecommissioningPhase(true);
    return nominatedWellDetail;
  }

  static NominatedWellDetailForm getValidForm() {
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases(false);
    form.setExplorationAndAppraisalPhase(true);
    form.setDevelopmentPhase(true);
    form.setDecommissioningPhase(true);
    return form;
  }
}
