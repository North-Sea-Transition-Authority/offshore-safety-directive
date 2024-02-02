package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.List;
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

  public List<Asset> getAssetsByPortalAssetIdsAndStatus(Collection<PortalAssetId> portalAssetIds,
                                                        PortalAssetType portalAssetType,
                                                        AssetStatus assetStatus) {
    var ids = portalAssetIds.stream()
        .map(PortalAssetId::id)
        .toList();
    return assetRepository.findAllByPortalAssetIdInAndPortalAssetTypeAndStatusIs(
        ids,
        portalAssetType,
        assetStatus
    );
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
