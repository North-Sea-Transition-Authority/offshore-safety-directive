package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;

@ExtendWith(MockitoExtension.class)
class AssetPersistenceServiceTest {

  @Mock
  private AssetRepository assetRepository;

  @InjectMocks
  private AssetPersistenceService assetPersistenceService;

  @Test
  void persistAssets() {
    var portalAssetType = PortalAssetType.INSTALLATION;
    var existingAsset = AssetTestUtil.builder()
        .withPortalAssetId("portal/asset/1")
        .withPortalAssetType(portalAssetType)
        .build();
    var newAssetId = new PortalAssetId("portal/asset/2");
    var existingAssetDto = new NominatedAssetDto(
        new PortalAssetId(existingAsset.getPortalAssetId()),
        portalAssetType,
        new AssetName(existingAsset.getAssetName()),
        List.of(InstallationPhase.DECOMMISSIONING.name())
    );

    var assetName = "asset name";

    var newAssetDto = new NominatedAssetDto(
        newAssetId,
        portalAssetType,
        new AssetName(assetName),
    List.of(InstallationPhase.DECOMMISSIONING.name())
    );

    when(assetRepository.findAllByPortalAssetIdIn(List.of(existingAsset.getPortalAssetId(), newAssetId.id())))
        .thenReturn(List.of(existingAsset));

    // Return collection that was passed into saveAll call.
    when(assetRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var assets = assetPersistenceService.persistNominatedAssets(List.of(existingAssetDto, newAssetDto));

    // Assert both missing and existing assets are returned
    assertThat(assets)
        .extracting(Asset::getPortalAssetId)
        .containsExactly(
            existingAsset.getPortalAssetId(),
            newAssetId.id()
        );

    // Verify only one asset was missing and saved
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Asset>> persistedAssetCaptor = ArgumentCaptor.forClass(List.class);
    verify(assetRepository).saveAll(persistedAssetCaptor.capture());

    assertThat(persistedAssetCaptor.getValue())
        .extracting(
            Asset::getPortalAssetId,
            Asset::getAssetName,
            Asset::getPortalAssetType
        )
        .containsExactly(
            tuple(newAssetId.id(), assetName, portalAssetType)
        );
  }

  @Test
  void persistNominatedAssets_whenNoExisting_verifySavedMissing() {
    var assetName = "asset name";
    var portalAssetType = PortalAssetType.INSTALLATION;
    var newAssetId = new PortalAssetId("portal/asset/1");
    var newAssetName = new AssetName(assetName);
    var newAssetDto = new NominatedAssetDto(
        newAssetId,
        portalAssetType,
        newAssetName,
        List.of(InstallationPhase.DECOMMISSIONING.name())
    );

    when(assetRepository.findAllByPortalAssetIdIn(List.of(newAssetId.id())))
        .thenReturn(List.of());

    // Return collection that was passed into saveAll call.
    when(assetRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var assets = assetPersistenceService.persistNominatedAssets(List.of(newAssetDto));

    // Assert both missing and existing assets are returned
    assertThat(assets)
        .extracting(Asset::getPortalAssetId)
        .containsExactly(
            newAssetId.id()
        );

    // Verify only one asset was missing and saved
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Asset>> persistedAssetCaptor = ArgumentCaptor.forClass(List.class);
    verify(assetRepository).saveAll(persistedAssetCaptor.capture());

    assertThat(persistedAssetCaptor.getValue())
        .extracting(
            Asset::getPortalAssetId,
            Asset::getAssetName,
            Asset::getPortalAssetType
        )
        .containsExactly(
            tuple(newAssetId.id(), assetName, portalAssetType)
        );
  }

}