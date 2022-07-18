package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Instant;

public class NominationDetailTestUtil {

  private NominationDetailTestUtil() {
    throw new IllegalStateException("NominationDetailTestUtil is an util class and should not be instantiated");
  }

  public static NominationDetail getNominationDetail() {
    var nominationDetail = new NominationDetail(1);
    nominationDetail.setNomination(new Nomination());
    nominationDetail.setCreatedInstant(Instant.now());
    nominationDetail.setVersion(1);
    nominationDetail.setStatus(NominationStatus.DRAFT);
    return nominationDetail;
  }
}
