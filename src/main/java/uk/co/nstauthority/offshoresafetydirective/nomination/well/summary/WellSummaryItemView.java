package uk.co.nstauthority.offshoresafetydirective.nomination.well.summary;

import java.util.Optional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreMechanicalStatus;

public record WellSummaryItemView(
    String name,
    WellboreId wellboreId,
    WellboreMechanicalStatus mechanicalStatus,
    LicenceDto originLicenceDto,
    LicenceDto totalDepthLicenceDto,
    boolean isOnPortal
) {

  public static WellSummaryItemView fromWellDto(WellDto wellDto) {
    return new WellSummaryItemView(
        Optional.ofNullable(wellDto.name()).orElse("Unknown wellbore"),
        wellDto.wellboreId(),
        wellDto.mechanicalStatus(),
        wellDto.originLicenceDto(),
        wellDto.totalDepthLicenceDto(),
        true
    );
  }

  public static WellSummaryItemView notOnPortal(String wellName, WellboreId wellboreId) {
    return new WellSummaryItemView(
        wellName,
        wellboreId,
        null,
        null,
        null,
        false
    );
  }

}
