package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

public class WellSelectionSetupTestUtil {

  private WellSelectionSetupTestUtil() {
    throw new IllegalStateException("WellSelectionSetupTestUtil is an util class and should not be instantiated");
  }

  static WellSelectionSetupForm getValidForm() {
    var form = new WellSelectionSetupForm();
    form.setWellSelectionType(WellSelectionType.NO_WELLS.name());
    return form;
  }

  public static WellSelectionSetup getWellSelectionSetup(NominationDetail nominationDetail) {
    var wellSetup = new WellSelectionSetup(1);
    wellSetup.setNominationDetail(nominationDetail);
    wellSetup.setSelectionType(WellSelectionType.NO_WELLS);
    return wellSetup;
  }
}
