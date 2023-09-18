package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class NominatedWellTestUtil {

  private NominatedWellTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static NominatedWell getNominatedWell(NominationDetail nominationDetail) {
    return new NominatedWell(nominationDetail, 1);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();

    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();

    private Integer wellboreId = 34;

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public Builder withWellboreId(Integer wellboreId) {
      this.wellboreId = wellboreId;
      return this;
    }

    public NominatedWell build() {
      var nominatedWell = new NominatedWell(id);
      nominatedWell.setNominationDetail(nominationDetail);
      nominatedWell.setWellId(wellboreId);
      return nominatedWell;
    }

  }
}
