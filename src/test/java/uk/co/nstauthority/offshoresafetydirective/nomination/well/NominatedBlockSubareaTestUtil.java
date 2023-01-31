package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class NominatedBlockSubareaTestUtil {

  private NominatedBlockSubareaTestUtil() {
    throw new IllegalStateException("NominatedBlockSubareaTestUtil is a test util and should not be instantiated");
  }

  static NominatedBlockSubareaBuilder builder() {
    return new NominatedBlockSubareaBuilder();
  }

  static class NominatedBlockSubareaBuilder {
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private String blockSubareaId = "subarea-id";

    private NominatedBlockSubareaBuilder() {}

    public NominatedBlockSubareaBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public NominatedBlockSubareaBuilder withBlockSubareaId(String blockSubareaId) {
      this.blockSubareaId = blockSubareaId;
      return this;
    }

    public NominatedBlockSubarea build() {
      var nominatedBlockSubarea = new NominatedBlockSubarea();
      nominatedBlockSubarea.setNominationDetail(nominationDetail);
      nominatedBlockSubarea.setBlockSubareaId(blockSubareaId);
      return nominatedBlockSubarea;
    }
  }
}
