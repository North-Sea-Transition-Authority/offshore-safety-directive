package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

public class WellSetupTestUtil {

  private WellSetupTestUtil() {
    throw new IllegalStateException("WellSetupTestUtil is an util class and should not be instantiated");
  }

  static WellSelectionSetupForm getValidForm() {
    var form = new WellSelectionSetupForm();
    form.setWellSelectionType(WellSelectionType.NO_WELLS.name());
    return form;
  }

  static WellSelectionSetup getWellSetup(NominationDetail nominationDetail) {
    var wellSetup = new WellSelectionSetup(1);
    wellSetup.setNominationDetail(nominationDetail);
    wellSetup.setSelectionType(WellSelectionType.NO_WELLS);
    return wellSetup;
  }
}
