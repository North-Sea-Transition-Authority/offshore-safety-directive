package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import java.util.Optional;
import uk.co.fivium.energyportalapi.generated.types.Wellbore;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;

public record WellDto(
    WellboreId wellboreId,
    String name,
    WellboreMechanicalStatus mechanicalStatus,
    LicenceDto originLicenceDto,
    LicenceDto totalDepthLicenceDto,
    WonsWellboreIntent intent
) {

  public static WellDto fromPortalWellbore(Wellbore wellbore) {

    LicenceDto originLicence = Optional.ofNullable(wellbore.getOriginLicence())
        .map(LicenceDto::fromPortalLicence)
        .orElse(null);

    LicenceDto totalDepthLicence = Optional.ofNullable(wellbore.getTotalDepthLicence())
        .map(LicenceDto::fromPortalLicence)
        .orElse(null);

    WonsWellboreIntent intent = Optional.ofNullable(wellbore.getIntent())
        .map(WonsWellboreIntent::fromPortalIntent)
        .orElse(null);

    return new WellDto(
        new WellboreId(wellbore.getId()),
        wellbore.getRegistrationNumber(),
        WellboreMechanicalStatus.fromPortalMechanicalStatus(wellbore.getMechanicalStatus()),
        originLicence,
        totalDepthLicence,
        intent
    );
  }
}
