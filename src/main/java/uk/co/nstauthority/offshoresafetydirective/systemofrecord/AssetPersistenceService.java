package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AssetPersistenceService {

  private final AssetRepository assetRepository;

  @Autowired
  AssetPersistenceService(AssetRepository assetRepository) {
    this.assetRepository = assetRepository;
  }

  @Transactional
  public List<Asset> persistNominatedAssets(Collection<NominatedAssetDto> nominatedAssetDtos) {
    var existingAssets = getExistingAssets(nominatedAssetDtos);

    List<Asset> missingAssets = createNonExistingAssets(nominatedAssetDtos, existingAssets);

    Stream<Asset> savedAssets = Stream.empty();
    if (!missingAssets.isEmpty()) {
      savedAssets = Streams.stream(assetRepository.saveAll(missingAssets));
    }

    return Stream.concat(existingAssets.stream(), savedAssets).toList();
  }

  private List<Asset> getExistingAssets(Collection<NominatedAssetDto> nominatedAssetDtos) {
    var allPortalAssetIds = nominatedAssetDtos.stream()
        .map(NominatedAssetDto::portalAssetId)
        .map(PortalAssetId::id)
        .toList();

    var allAssets = assetRepository.findAllByPortalAssetIdIn(allPortalAssetIds);

    return allAssets.stream()
        .filter(asset ->
            nominatedAssetDtos.stream().anyMatch(nominatedAssetDto ->
                nominatedAssetDto.portalAssetType().equals(asset.getPortalAssetType())
                    && nominatedAssetDto.portalAssetId().id().equals(asset.getPortalAssetId())
            )
        )
        .toList();
  }

  private List<Asset> createNonExistingAssets(
      Collection<NominatedAssetDto> nominatedAssetDtos,
      List<Asset> existingAssets
  ) {
    return nominatedAssetDtos.stream()
        .filter(nominatedAssetDto ->
            existingAssets.stream().noneMatch(asset ->
                asset.getPortalAssetType().equals(nominatedAssetDto.portalAssetType())
                    && asset.getPortalAssetId().equals(nominatedAssetDto.portalAssetId().id())
            )
        )
        .map(this::createAssetEntity)
        .toList();
  }

  private Asset createAssetEntity(NominatedAssetDto nominatedAssetDto) {
    var asset = new Asset();
    // TODO OSDOP-341 - Use names attached to portal asset
    asset.setAssetName("PLACEHOLDER");
    asset.setPortalAssetId(nominatedAssetDto.portalAssetId().id());
    asset.setPortalAssetType(nominatedAssetDto.portalAssetType());
    return asset;
  }

}
