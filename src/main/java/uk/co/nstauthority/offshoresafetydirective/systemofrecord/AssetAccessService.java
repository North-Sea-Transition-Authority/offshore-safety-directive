package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetAccessService {

  private final AssetRepository assetRepository;

  @Autowired
  public AssetAccessService(AssetRepository assetRepository) {
    this.assetRepository = assetRepository;
  }

  public Optional<AssetDto> getAsset(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    return assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(
            portalAssetId.id(),
            portalAssetType,
            AssetStatus.EXTANT)
        .map(AssetDto::fromAsset);
  }

  public Optional<AssetDto> getAsset(AssetId assetId) {
    return assetRepository.findById(assetId.id())
        .map(AssetDto::fromAsset);
  }

  public boolean isAssetExtant(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    return assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(
            portalAssetId.id(),
            portalAssetType,
            AssetStatus.EXTANT
        )
        .map(asset -> AssetStatus.EXTANT.equals(asset.getStatus()))
        .orElse(false);
  }
}
