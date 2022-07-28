package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

public class WellTestUtil {

  private WellTestUtil() {
    throw new IllegalStateException("WellTestUtil is a util class and should not be instantiated");
  }

  public static Well getWell(NominationDetail nominationDetail) {
    return new Well(nominationDetail, 1);
  }
}
