package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class NominatedBlockSubareaTestUtil {

  private NominatedBlockSubareaTestUtil() {
    throw new IllegalStateException("NominatedBlockSubareaTestUtil is a test util and should not be instantiated");
  }

  public static class NominatedBlockSubareaBuilder {
    private NominationDetail nominationDetail = NominationDetailTestUtil.getNominationDetail();
    private Integer blockSubareaId = 1;

    public NominatedBlockSubareaBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public NominatedBlockSubareaBuilder withBlockSubareaId(Integer blockSubareaId) {
      this.blockSubareaId = blockSubareaId;
      return this;
    }

    public NominatedBlockSubarea build() {
      return new NominatedBlockSubarea()
          .setNominationDetail(nominationDetail)
          .setBlockSubareaId(blockSubareaId);
    }
  }
}
