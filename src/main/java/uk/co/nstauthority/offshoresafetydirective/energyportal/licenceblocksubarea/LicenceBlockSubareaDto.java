package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import uk.co.fivium.energyportalapi.generated.types.Subarea;

public record LicenceBlockSubareaDto(
    LicenceBlockSubareaId subareaId,
    SubareaName subareaName,
    LicenceBlock licenceBlock,
    Licence licence
) {

  static LicenceBlockSubareaDto fromPortalSubarea(Subarea subarea) {

    var licence = subarea.getLicence();

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
        new Licence(
            new Licence.LicenceType(licence.getLicenceType()),
            new Licence.LicenceNumber(licence.getLicenceNo()),
            new Licence.LicenceReference(licence.getLicenceRef())
        )
    );
  }

  public String displayName() {
    return "%s %s %s".formatted(
        licence.licenceReference().value(),
        licenceBlock.reference().value(),
        subareaName.value()
    );
  }

  public static LicenceBlockSubareaComparator sort() {
    return new LicenceBlockSubareaComparator();
  }
}
