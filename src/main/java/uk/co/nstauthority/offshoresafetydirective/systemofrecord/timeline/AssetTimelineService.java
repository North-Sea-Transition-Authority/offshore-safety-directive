package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
class AssetTimelineService {

  private final PortalAssetNameService portalAssetNameService;
  private final AssetAccessService assetAccessService;
  private final AppointmentAccessService appointmentAccessService;
  private final AppointmentTimelineItemService appointmentTimelineItemService;
  private final TerminationTimelineItemService terminationTimelineItemService;

  @Autowired
  AssetTimelineService(PortalAssetNameService portalAssetNameService, AssetAccessService assetAccessService,
                       AppointmentAccessService appointmentAccessService,
                       AppointmentTimelineItemService appointmentTimelineItemService,
                       TerminationTimelineItemService terminationTimelineItemService) {
    this.portalAssetNameService = portalAssetNameService;
    this.assetAccessService = assetAccessService;
    this.appointmentAccessService = appointmentAccessService;
    this.appointmentTimelineItemService = appointmentTimelineItemService;
    this.terminationTimelineItemService = terminationTimelineItemService;
  }

  Optional<AssetAppointmentHistory> getAppointmentHistoryForPortalAsset(PortalAssetId portalAssetId,
                                                                        PortalAssetType portalAssetType) {

    Optional<AssetName> energyPortalAssetName = portalAssetNameService.getAssetName(portalAssetId, portalAssetType);

    Optional<AssetDto> assetOptional = assetAccessService.getAsset(portalAssetId);

    if (assetOptional.isEmpty() && energyPortalAssetName.isEmpty()) {
      return Optional.empty();
    }

    List<AssetTimelineItemView> timelineItemViews = new ArrayList<>();
    AssetName cachedAssetName = null;

    if (assetOptional.isPresent()) {

      var assetDto = assetOptional.get();

      var appointments = appointmentAccessService.getAppointmentsForAsset(assetDto.assetId());
      var appointmentDtos = appointments.stream()
          .map(AppointmentDto::fromAppointment)
          .toList();

      if (!appointmentDtos.isEmpty()) {
        timelineItemViews.addAll(appointmentTimelineItemService.getTimelineItemViews(appointmentDtos, assetDto));
      }

      if (!CollectionUtils.isEmpty(appointments)) {
        timelineItemViews.addAll(terminationTimelineItemService.getTimelineItemViews(appointments));
      }

      cachedAssetName = assetDto.assetName();
    }

    timelineItemViews = timelineItemViews.stream()
        .sorted(
            Comparator.comparing(timelineItem -> ((AssetTimelineItemView) timelineItem).eventDate())
                .thenComparing(timelineItem -> ((AssetTimelineItemView) timelineItem).createdInstant())
                .reversed()
        )
        .toList();

    AssetName assetName = energyPortalAssetName.orElse(cachedAssetName);

    return Optional.of(new AssetAppointmentHistory(assetName, timelineItemViews));
  }

}
