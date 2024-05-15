package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import uk.co.fivium.energyportalapi.generated.types.Licence;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;

public record LicenceDto(
    LicenceId licenceId,
    LicenceType licenceType,
    LicenceNumber licenceNumber,
    LicenceReference licenceReference,
    Set<OrganisationUnit> licensees
) {

  public record LicenceType(String value) {}

  public record LicenceNumber(Integer value) {}

  public record LicenceReference(String value) {}

  public static LicenceDto fromPortalLicence(Licence portalLicence) {
    var licensees = Optional.ofNullable(portalLicence.getLicensees()).orElse(List.of());
    return new LicenceDto(
        new LicenceId(portalLicence.getId()),
        new LicenceType(portalLicence.getLicenceType()),
        new LicenceNumber(portalLicence.getLicenceNo()),
        new LicenceReference(portalLicence.getLicenceRef()),
        new HashSet<>(licensees)
    );
  }

  public static LicenceComparator sort() {
    return new LicenceComparator();
  }
}
