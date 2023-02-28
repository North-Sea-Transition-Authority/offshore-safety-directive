package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
class AppointmentTimelineService {

  private final PortalAssetNameService portalAssetNameService;

  private final AssetAccessService assetAccessService;

  @Autowired
  AppointmentTimelineService(PortalAssetNameService portalAssetNameService, AssetAccessService assetAccessService) {
    this.portalAssetNameService = portalAssetNameService;
    this.assetAccessService = assetAccessService;
  }

  Optional<AssetAppointmentHistory> getAppointmentHistoryForPortalAsset(PortalAssetId portalAssetId,
                                                                        PortalAssetType portalAssetType) {

    Optional<AssetName> energyPortalAssetName = portalAssetNameService.getAssetName(portalAssetId, portalAssetType);

    Optional<AssetDto> assetOptional = assetAccessService.getAsset(portalAssetId);

    if (assetOptional.isEmpty() && energyPortalAssetName.isEmpty()) {
      return Optional.empty();
    }

    AssetName cachedAssetName = null;

    if (assetOptional.isPresent()) {

      var asset = assetOptional.get();

      cachedAssetName = asset.assetName();
    }

    AssetName assetName = energyPortalAssetName.orElse(cachedAssetName);

    return Optional.of(new AssetAppointmentHistory(assetName));
  }
}
