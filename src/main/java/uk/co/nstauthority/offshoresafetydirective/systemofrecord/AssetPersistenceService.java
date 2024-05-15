package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;

@Service
public class AssetPersistenceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssetPersistenceService.class);

  private final AssetRepository assetRepository;
  private final PortalAssetRetrievalService portalAssetRetrievalService;
  private final AssetAccessService assetAccessService;

  @Autowired
  public AssetPersistenceService(AssetRepository assetRepository, PortalAssetRetrievalService portalAssetRetrievalService,
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
  public void endAssetsWithAssetType(Collection<PortalAssetId> portalAssetIds, PortalAssetType portalAssetType,
                                     String portalEventId) {

    var ids = portalAssetIds.stream()
        .map(PortalAssetId::id)
        .toList();

    var assets = assetRepository.findByPortalAssetIdInAndPortalAssetType(ids, portalAssetType);

    var portalAssetIdsString = String.join(",", ids);
    var retrievedAssetIdsString = assets.stream()
        .map(Asset::getId)
        .map(UUID::toString)
        .collect(Collectors.joining(","));

    LOGGER.info(
        "Correlated PortalEventId [{}] - Ending assets [{}] for PortalAssetIds [{}]",
        portalEventId,
        retrievedAssetIdsString,
        portalAssetIdsString
    );

    assets.forEach(asset -> {
      asset.setStatus(AssetStatus.REMOVED);
      asset.setPortalEventType(PortalEventType.PEARS_TRANSACTION_OPERATION);
      asset.setPortalEventId(portalEventId);
    });

    assetRepository.saveAll(assets);
  }

  @Transactional
  public Asset getOrCreateAsset(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {

    var existingAsset = assetAccessService.getExtantAsset(portalAssetId.id(), portalAssetType);

    var assetName = existingAsset
        .map(Asset::getAssetName)
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

      return persistNominatedAsset(nominatedAssetDto);
    });
  }

  @Transactional
  public List<Asset> createAssetsForSubareas(Collection<LicenceBlockSubareaDto> subareaDtos) {

    var subareaIds = subareaDtos.stream()
        .map(dto -> dto.subareaId().id())
        .toList();

    var existingAssets = assetRepository.findAllByPortalAssetIdIn(subareaIds);

    var subareaDtosAsAssets = subareaDtos.stream()
        .filter(dto -> existingAssets.stream()
            .noneMatch(asset -> asset.getPortalAssetId().equals(dto.subareaId().id()))
        )
        .map(licenceBlockSubareaDto -> {
          var asset = new Asset();
          asset.setPortalAssetId(licenceBlockSubareaDto.subareaId().id());
          asset.setPortalAssetType(PortalAssetType.SUBAREA);
          asset.setAssetName(licenceBlockSubareaDto.displayName());
          asset.setStatus(AssetStatus.EXTANT);
          return asset;
        })
        .toList();

    return Lists.newArrayList(assetRepository.saveAll(subareaDtosAsAssets));
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

    var assetName = Optional.ofNullable(nominatedAssetDto.portalAssetName().value())
        .orElse("Unknown %s".formatted(nominatedAssetDto.portalAssetType().getSentenceCaseDisplayName()));

    var asset = new Asset();
    asset.setAssetName(assetName);
    asset.setPortalAssetId(nominatedAssetDto.portalAssetId().id());
    asset.setPortalAssetType(nominatedAssetDto.portalAssetType());
    asset.setStatus(AssetStatus.EXTANT);
    return asset;
  }

}
