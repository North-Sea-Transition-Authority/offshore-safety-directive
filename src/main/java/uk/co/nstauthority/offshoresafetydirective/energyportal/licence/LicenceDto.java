package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import uk.co.fivium.energyportalapi.generated.types.Licence;

public record LicenceDto(
    LicenceId licenceId,
    LicenceType licenceType,
    LicenceNumber licenceNumber,
    LicenceReference licenceReference
) {

  public record LicenceType(String value) {}

  public record LicenceNumber(Integer value) {}

  public record LicenceReference(String value) {}

  public static LicenceDto fromPortalLicence(Licence portalLicence) {
    return new LicenceDto(
        new LicenceId(portalLicence.getId()),
        new LicenceType(portalLicence.getLicenceType()),
        new LicenceNumber(portalLicence.getLicenceNo()),
        new LicenceReference(portalLicence.getLicenceRef())
    );
  }

  public static LicenceComparator sort() {
    return new LicenceComparator();
  }
}
