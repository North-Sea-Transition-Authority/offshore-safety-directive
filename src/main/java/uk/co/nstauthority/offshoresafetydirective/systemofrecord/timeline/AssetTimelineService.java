package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.ArrayList;
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

  @Autowired
  AssetTimelineService(PortalAssetNameService portalAssetNameService, AssetAccessService assetAccessService,
                       AppointmentAccessService appointmentAccessService,
                       AppointmentTimelineItemService appointmentTimelineItemService) {
    this.portalAssetNameService = portalAssetNameService;
    this.assetAccessService = assetAccessService;
    this.appointmentAccessService = appointmentAccessService;
    this.appointmentTimelineItemService = appointmentTimelineItemService;
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

      List<AppointmentDto> appointments = appointmentAccessService.getAppointmentsForAsset(assetDto.assetId())
          .stream()
          .toList();

      if (!CollectionUtils.isEmpty(appointments)) {
        timelineItemViews.addAll(appointmentTimelineItemService.getTimelineItemViews(appointments, assetDto));
      }

      cachedAssetName = assetDto.assetName();
    }

    AssetName assetName = energyPortalAssetName.orElse(cachedAssetName);

    return Optional.of(new AssetAppointmentHistory(assetName, timelineItemViews));
  }

}
