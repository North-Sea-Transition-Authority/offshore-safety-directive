package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

record LicenceBlock(
    QuadrantNumber quadrantNumber,
    BlockNumber blockNumber,
    BlockSuffix blockSuffix,
    BlockReference reference
) {

  record QuadrantNumber(String value) {}

  record BlockNumber(Integer value) {}

  record BlockSuffix(String value) {}

  record BlockReference(String value) {}
}
