package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import uk.co.fivium.energyportalapi.generated.types.Subarea;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;

public class LicenceBlockSubareaDto extends SubareaDto {

  private final SubareaName subareaName;
  private final LicenceBlock licenceBlock;
  private final LicenceDto licenceDto;

  public LicenceBlockSubareaDto(LicenceBlockSubareaId subareaId,
                                SubareaName subareaName,
                                LicenceBlock licenceBlock,
                                LicenceDto licenceDto) {
    super(subareaId);
    this.subareaName = subareaName;
    this.licenceBlock = licenceBlock;
    this.licenceDto = licenceDto;
  }

  static LicenceBlockSubareaDto fromPortalSubarea(Subarea subarea) {

    var licenceBlock = subarea.getLicenceBlock();

    return new LicenceBlockSubareaDto(
        new LicenceBlockSubareaId(subarea.getId()),
        new SubareaName(subarea.getName()),
        new LicenceBlock(
            new LicenceBlock.QuadrantNumber(licenceBlock.getQuadrantNumber()),
            new LicenceBlock.BlockNumber(licenceBlock.getBlockNumber()),
            new LicenceBlock.BlockSuffix(licenceBlock.getSuffix()),
            new LicenceBlock.BlockReference(licenceBlock.getReference())
        ),
        LicenceDto.fromPortalLicence(subarea.getLicence())
    );
  }

  public SubareaName subareaName() {
    return subareaName;
  }

  public LicenceBlock licenceBlock() {
    return licenceBlock;
  }

  public LicenceDto licence() {
    return licenceDto;
  }

  public String displayName() {
    return "%s %s %s".formatted(
        licenceDto.licenceReference().value(),
        licenceBlock.reference().value(),
        subareaName.value()
    );
  }

  public static LicenceBlockSubareaComparator sort() {
    return new LicenceBlockSubareaComparator();
  }
}
