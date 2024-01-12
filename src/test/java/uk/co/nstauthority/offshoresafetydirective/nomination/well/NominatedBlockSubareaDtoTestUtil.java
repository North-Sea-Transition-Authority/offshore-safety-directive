package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominatedBlockSubareaDtoTestUtil {

  private NominatedBlockSubareaDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private LicenceBlockSubareaId subareaId = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    private String name = "subarea name %s".formatted(UUID.randomUUID());

    private Builder() {
    }

    public Builder withSubareaId(LicenceBlockSubareaId subareaId) {
      this.subareaId = subareaId;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public NominatedBlockSubareaDto build() {
      return new NominatedBlockSubareaDto(
          subareaId,
          name
      );
    }

  }
}