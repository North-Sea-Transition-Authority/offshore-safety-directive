package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import uk.co.fivium.energyportalapi.generated.types.Wellbore;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;

public record WellDto(
    WellboreId wellboreId,
    String name,
    WellboreMechanicalStatus mechanicalStatus,
    LicenceDto originLicenceDto,
    LicenceDto totalDepthLicenceDto
) {

  public static WellDto fromPortalWellbore(Wellbore wellbore) {

    var originLicence = wellbore.getOriginLicence();
    LicenceDto originLicenceDto = null;

    var totalDepthLicence = wellbore.getTotalDepthLicence();
    LicenceDto totalDepthLicenceDto = null;

    if (originLicence != null) {
      originLicenceDto = LicenceDto.fromPortalLicence(originLicence);
    }

    if (totalDepthLicence != null) {
      totalDepthLicenceDto = LicenceDto.fromPortalLicence(wellbore.getTotalDepthLicence());
    }

    return new WellDto(
        new WellboreId(wellbore.getId()),
        wellbore.getRegistrationNumber(),
        WellboreMechanicalStatus.fromPortalMechanicalStatus(wellbore.getMechanicalStatus()),
        originLicenceDto,
        totalDepthLicenceDto
    );
  }
}
