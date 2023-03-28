package uk.co.nstauthority.offshoresafetydirective.nomination;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominationDtoTestUtil {

  private NominationDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private NominationId nominationId = new NominationId(150);

    private String nominationReference = "nomination reference";

    public Builder withNominationId(NominationId nominationId) {
      this.nominationId = nominationId;
      return this;
    }

    public Builder withNominationId(Integer nominationId) {
      return withNominationId(new NominationId(nominationId));
    }

    public Builder withNominationReference(String nominationReference) {
      this.nominationReference = nominationReference;
      return this;
    }

    public NominationDto build() {
      return new NominationDto(nominationId, nominationReference);
    }

  }
}
