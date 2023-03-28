package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class LicenceBlockSubareaDtoTestUtil {

  private LicenceBlockSubareaDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String subareaId = "subarea id";

    private String subareaName = "subarea name";

    private String quadrantNumber = "quadrant number";

    private Integer blockNumber = 10;

    private String blockSuffix = "block suffix";

    private String blockReference = "block reference";

    private String licenceType = "licence type";

    private Integer licenceNumber = 20;

    private String licenceReference = "licence reference";

    private boolean isExtant = true;

    private Builder() {}

    public Builder withSubareaId(String subareaId) {
      this.subareaId = subareaId;
      return this;
    }

    public Builder withSubareaName(String subareaName) {
      this.subareaName = subareaName;
      return this;
    }

    public Builder withQuadrantNumber(String quadrantNumber) {
      this.quadrantNumber = quadrantNumber;
      return this;
    }

    public Builder withBlockNumber(Integer blockNumber) {
      this.blockNumber = blockNumber;
      return this;
    }

    public Builder withBlockSuffix(String blockSuffix) {
      this.blockSuffix = blockSuffix;
      return this;
    }

    public Builder withBlockReference(String blockReference) {
      this.blockReference = blockReference;
      return this;
    }

    public Builder withLicenceType(String licenceType) {
      this.licenceType = licenceType;
      return this;
    }

    public Builder withLicenceNumber(Integer licenceNumber) {
      this.licenceNumber = licenceNumber;
      return this;
    }

    public Builder withLicenceReference(String licenceReference) {
      this.licenceReference = licenceReference;
      return this;
    }

    public Builder isExtant(boolean isExtant) {
      this.isExtant = isExtant;
      return this;
    }

    public LicenceBlockSubareaDto build() {
      return new LicenceBlockSubareaDto(
          new LicenceBlockSubareaId(subareaId),
          new SubareaName(subareaName),
          new LicenceBlock(
              new LicenceBlock.QuadrantNumber(quadrantNumber),
              new LicenceBlock.BlockNumber(blockNumber),
              new LicenceBlock.BlockSuffix(blockSuffix),
              new LicenceBlock.BlockReference(blockReference)
          ),
          new LicenceDto(
              new LicenceDto.LicenceType(licenceType),
              new LicenceDto.LicenceNumber(licenceNumber),
              new LicenceDto.LicenceReference(licenceReference)
          ),
          isExtant
      );
    }
  }
}
