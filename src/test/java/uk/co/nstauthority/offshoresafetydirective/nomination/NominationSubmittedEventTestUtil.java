package uk.co.nstauthority.offshoresafetydirective.nomination;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominationSubmittedEventTestUtil {

  private NominationSubmittedEventTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static NominationSubmittedEvent createEvent(NominationDetail nominationDetail) {
    return new NominationSubmittedEvent(new Object(), nominationDetail);
  }

}