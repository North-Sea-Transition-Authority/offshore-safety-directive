package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetPersistenceService {

  private final AssetRepository assetRepository;
  private final PortalAssetRetrievalService portalAssetRetrievalService;
  private final AssetAccessService assetAccessService;

  @Autowired
  AssetPersistenceService(AssetRepository assetRepository, PortalAssetRetrievalService portalAssetRetrievalService,
                          AssetAccessService assetAccessService) {
    this.assetRepository = assetRepository;
    this.portalAssetRetrievalService = portalAssetRetrievalService;
    this.assetAccessService = assetAccessService;
  }

  @Transactional
  public List<Asset> persistNominatedAssets(Collection<NominatedAssetDto> nominatedAssetDtos) {
    var existingExtantAssets = getExistingAssets(nominatedAssetDtos);

    List<Asset> missingAssets = createNonExistingAssets(nominatedAssetDtos, existingExtantAssets);

    Stream<Asset> savedAssets = Stream.empty();
    if (!missingAssets.isEmpty()) {
      savedAssets = Streams.stream(assetRepository.saveAll(missingAssets));
    }

    return Stream.concat(existingExtantAssets.stream(), savedAssets).toList();
  }

  @Transactional
  public Asset persistNominatedAsset(NominatedAssetDto nominatedAssetDto) {
    var asset = createAssetEntity(nominatedAssetDto);
    return assetRepository.save(asset);
  }

  @Transactional
  public AssetDto getOrCreateAsset(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {

    var existingAsset = assetAccessService.getAsset(portalAssetId, portalAssetType);

    var assetName = existingAsset
        .map(AssetDto::assetName)
        .map(AssetName::value)
        .or(() -> portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .orElseThrow(() -> new IllegalStateException(
            "No portal asset of type [%s] found with ID [%s]".formatted(
                portalAssetType,
                portalAssetId.id()
            )
        ));

    return existingAsset.orElseGet(() -> {
      var nominatedAssetDto = new NominatedAssetDto(
          portalAssetId,
          portalAssetType,
          new AssetName(assetName)
      );

      var persistedAsset = persistNominatedAsset(nominatedAssetDto);
      return AssetDto.fromAsset(persistedAsset);
    });
  }

  private List<Asset> getExistingAssets(Collection<NominatedAssetDto> nominatedAssetDtos) {
    var allPortalAssetIds = nominatedAssetDtos.stream()
        .map(NominatedAssetDto::portalAssetId)
        .map(PortalAssetId::id)
        .toList();

    var allAssets = assetRepository.findAllByPortalAssetIdInAndStatusIs(allPortalAssetIds, AssetStatus.EXTANT);

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
    asset.setAssetName(nominatedAssetDto.portalAssetName().value());
    asset.setPortalAssetId(nominatedAssetDto.portalAssetId().id());
    asset.setPortalAssetType(nominatedAssetDto.portalAssetType());
    asset.setStatus(AssetStatus.EXTANT);
    return asset;
  }

}
