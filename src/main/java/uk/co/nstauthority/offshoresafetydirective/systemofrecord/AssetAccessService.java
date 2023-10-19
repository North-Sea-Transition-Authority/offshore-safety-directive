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

  public Optional<AssetDto> getAsset(PortalAssetId assetId, PortalAssetType portalAssetType) {
    return assetRepository.findByPortalAssetIdAndPortalAssetType(assetId.id(), portalAssetType)
        .map(AssetDto::fromAsset);
  }

  public Optional<AssetDto> getAsset(AssetId assetId) {
    return assetRepository.findById(assetId.id())
        .map(AssetDto::fromAsset);
  }
}
