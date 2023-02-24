package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailView;

@Service
class AssetPersistenceService {

  private final AssetRepository assetRepository;

  @Autowired
  AssetPersistenceService(AssetRepository assetRepository) {
    this.assetRepository = assetRepository;
  }

  @Transactional
  public List<Asset> getExistingOrCreateAssets(NominatedInstallationDetailView installationDetailView) {
    var assetIds = installationDetailView.getInstallations()
        .stream()
        .map(dto -> String.valueOf(dto.id()))
        .toList();

    var retrievedAssets = assetRepository.findAllByPortalAssetIdIn(assetIds);

    var missingAssets = installationDetailView.getInstallations()
        .stream()
        .filter(dto ->
            retrievedAssets.stream()
                .noneMatch(asset -> asset.getPortalAssetId().equals(String.valueOf(dto.id())))
        )
        .map(this::createAssetFromDto)
        .toList();

    if (!missingAssets.isEmpty()) {
      missingAssets = ImmutableList.copyOf(assetRepository.saveAll(missingAssets));
    }

    return Stream.concat(retrievedAssets.stream(), missingAssets.stream()).toList();
  }

  private Asset createAssetFromDto(InstallationDto dto) {
    var asset = new Asset();
    // TODO OSDOP-341 - Use names attached to installation entity
    asset.setAssetName("PLACEHOLDER");
    asset.setPortalAssetId(String.valueOf(dto.id()));
    asset.setPortalAssetType(PortalAssetType.INSTALLATION);
    return asset;
  }

}
