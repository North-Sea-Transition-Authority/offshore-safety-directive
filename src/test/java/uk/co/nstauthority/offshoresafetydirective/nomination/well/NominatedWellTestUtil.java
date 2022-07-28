package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

public class NominatedWellTestUtil {

  private NominatedWellTestUtil() {
    throw new IllegalStateException("NominatedWellTestUtil is a util class and should not be instantiated");
  }

  public static NominatedWell getNominatedWell(NominationDetail nominationDetail) {
    return new NominatedWell(nominationDetail, 1);
  }
}
