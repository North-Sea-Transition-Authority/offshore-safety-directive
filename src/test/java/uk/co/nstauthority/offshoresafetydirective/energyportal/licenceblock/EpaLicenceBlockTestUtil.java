package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblock;

import uk.co.fivium.energyportalapi.generated.types.LicenceBlock;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class EpaLicenceBlockTestUtil {

  private EpaLicenceBlockTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String quadrantNumber = "quadrant number";

    private Integer blockNumber = 10;

    private String suffix = "suffix";

    private String reference = "reference";

    private Builder() {}

    public Builder withQuadrantNumber(String quadrantNumber) {
      this.quadrantNumber = quadrantNumber;
      return this;
    }

    public Builder withBlockNumber(Integer blockNumber) {
      this.blockNumber = blockNumber;
      return this;
    }

    public Builder withBlockSuffix(String blockSuffix) {
      this.suffix = blockSuffix;
      return this;
    }

    public Builder withReference(String reference) {
      this.reference = reference;
      return this;
    }

    public LicenceBlock build() {
      return LicenceBlock.newBuilder()
          .quadrantNumber(quadrantNumber)
          .blockNumber(blockNumber)
          .suffix(suffix)
          .reference(reference)
          .build();
    }

  }
}
