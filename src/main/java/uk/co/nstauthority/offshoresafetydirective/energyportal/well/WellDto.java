package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Wellbore;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;

public record WellDto(
    WellboreId wellboreId,
    String name,
    WellboreMechanicalStatus mechanicalStatus,
    List<LicenceDto> relatedLicences
) {

  public static WellDto fromPortalWellbore(Wellbore wellbore) {

    List<LicenceDto> relatedLicences = new ArrayList<>();

    var originLicence = wellbore.getOriginLicence();
    var totalDepthLicence = wellbore.getTotalDepthLicence();

    if (originLicence != null) {
      relatedLicences.add(LicenceDto.fromPortalLicence(originLicence));
    }

    if (totalDepthLicence != null
        && originLicence != null
        && !totalDepthLicence.getLicenceRef().equals(originLicence.getLicenceRef())
    ) {
      relatedLicences.add(LicenceDto.fromPortalLicence(wellbore.getTotalDepthLicence()));
    }

    return new WellDto(
        new WellboreId(wellbore.getId()),
        wellbore.getRegistrationNumber(),
        WellboreMechanicalStatus.fromPortalMechanicalStatus(wellbore.getMechanicalStatus()),
        relatedLicences
            .stream()
            .sorted(LicenceDto.sort())
            .toList()
    );
  }
}
