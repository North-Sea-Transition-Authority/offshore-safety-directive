package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.UUID;
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
    private UUID id = UUID.randomUUID();
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private String blockSubareaId = "subarea-id";
    private String name = "name-%s".formatted(UUID.randomUUID());

    private NominatedBlockSubareaBuilder() {}

    NominatedBlockSubareaBuilder withId(UUID id) {
      this.id = id;
      return this;
    }

    public NominatedBlockSubareaBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public NominatedBlockSubareaBuilder withBlockSubareaId(String blockSubareaId) {
      this.blockSubareaId = blockSubareaId;
      return this;
    }

    public NominatedBlockSubareaBuilder withName(String name) {
      this.name = name;
      return this;
    }

    public NominatedBlockSubarea build() {
      var nominatedBlockSubarea = new NominatedBlockSubarea(id);
      nominatedBlockSubarea.setNominationDetail(nominationDetail);
      nominatedBlockSubarea.setBlockSubareaId(blockSubareaId);
      nominatedBlockSubarea.setName(name);
      return nominatedBlockSubarea;
    }
  }
}
