package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class AssetPersistenceServiceTest {

  @Mock
  private AssetRepository assetRepository;

  @Mock
  private PortalAssetRetrievalService portalAssetRetrievalService;

  @Mock
  private AssetAccessService assetAccessService;

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

  @Test
  void getOrCreateAsset_whenAssetNameNotResolvable_thenError() {
    var portalAssetId = new PortalAssetId("123");
    var portalAssetType = PortalAssetType.INSTALLATION;

    when(assetAccessService.getAsset(portalAssetId, portalAssetType))
        .thenReturn(Optional.empty());

    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No portal asset of type [%s] found with ID [%s]".formatted(
                portalAssetType,
                portalAssetId.id()
            ));
  }

  @Test
  void getOrCreateAsset_whenNoExistingAsset_thenAssetCreated() {
    var portalAssetId = new PortalAssetId("123");
    var portalAssetType = PortalAssetType.INSTALLATION;

    when(assetAccessService.getAsset(portalAssetId, portalAssetType))
        .thenReturn(Optional.empty());

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.of(assetName));

    doAnswer(invocation -> invocation.getArgument(0))
        .when(assetRepository)
        .save(any());

    var result = assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType);

    assertThat(result)
        .extracting(
            AssetDto::portalAssetId,
            AssetDto::portalAssetType,
            assetDto -> assetDto.assetName().value()
        )
        .containsExactly(
            portalAssetId,
            portalAssetType,
            assetName
        );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Asset> savedAsset = ArgumentCaptor.forClass(Asset.class);
    verify(assetRepository).save(savedAsset.capture());

    assertThat(savedAsset.getValue())
        .extracting(
            Asset::getPortalAssetId,
            Asset::getPortalAssetType,
            Asset::getAssetName
        )
        .containsExactly(
            portalAssetId.id(),
            portalAssetType,
            assetName
        );

  }

  @Test
  void getOrCreateAsset_whenHasExistingAsset_thenAssetDtoReturned() {
    var portalAssetId = new PortalAssetId("123");
    var portalAssetType = PortalAssetType.INSTALLATION;
    var assetName = "asset name";

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetId(portalAssetId.id())
        .withPortalAssetType(portalAssetType)
        .withAssetName(assetName)
        .build();

    when(assetAccessService.getAsset(portalAssetId, portalAssetType))
        .thenReturn(Optional.of(assetDto));

    var result = assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType);

    assertThat(result)
        .extracting(
            AssetDto::portalAssetId,
            AssetDto::portalAssetType,
            dto -> dto.assetName().value()
        )
        .containsExactly(
            portalAssetId,
            portalAssetType,
            assetName
        );

    verify(assetRepository, never()).saveAll(any());
    verifyNoInteractions(portalAssetRetrievalService);
  }

  @Test
  void persistNominatedAsset() {
    var portalAssetId = new PortalAssetId("123");
    var portalAssetType = PortalAssetType.INSTALLATION;
    var assetName = new AssetName("asset name");

    var nominatedAssetDto = NominatedAssetDtoTestUtil.builder()
        .withPortalAssetId(portalAssetId)
        .withPortalAssetType(portalAssetType)
        .withPortalAssetName(assetName)
        .build();

    var captor = ArgumentCaptor.forClass(Asset.class);
    doAnswer(invocation -> invocation.getArgument(0))
        .when(assetRepository)
        .save(captor.capture());

    var result = assetPersistenceService.persistNominatedAsset(nominatedAssetDto);

    assertThat(result).isEqualTo(captor.getValue());

    assertThat(captor.getValue())
        .extracting(
            Asset::getPortalAssetId,
            Asset::getPortalAssetType,
            Asset::getAssetName
        )
        .containsExactly(
            portalAssetId.id(),
            portalAssetType,
            assetName.value()
        );
  }

}