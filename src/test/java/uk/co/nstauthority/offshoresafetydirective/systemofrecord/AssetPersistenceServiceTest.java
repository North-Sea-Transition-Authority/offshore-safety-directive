package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;

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

    when(assetRepository.findAllByPortalAssetIdInAndStatusIs(List.of(existingAsset.getPortalAssetId(), newAssetId.id()), AssetStatus.EXTANT))
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
            Asset::getPortalAssetType,
            Asset::getStatus
        )
        .containsExactly(
            tuple(newAssetId.id(), assetName, portalAssetType, AssetStatus.EXTANT)
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

    when(assetRepository.findAllByPortalAssetIdInAndStatusIs(List.of(newAssetId.id()), AssetStatus.EXTANT))
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
            Asset::getPortalAssetType,
            Asset::getStatus
        )
        .containsExactly(
            tuple(newAssetId.id(), assetName, portalAssetType, AssetStatus.EXTANT)
        );
  }

  @Test
  void getOrCreateAsset_whenAssetNameNotResolvable_thenError() {
    var portalAssetId = new PortalAssetId("123");
    var portalAssetType = PortalAssetType.INSTALLATION;

    when(assetAccessService.getExtantAsset(portalAssetId.id(), portalAssetType))
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

    when(assetAccessService.getExtantAsset(portalAssetId.id(), portalAssetType))
        .thenReturn(Optional.empty());

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.of(assetName));

    doAnswer(invocation -> invocation.getArgument(0))
        .when(assetRepository)
        .save(any(Asset.class));

    var result = assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType);

    assertThat(result)
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

    ArgumentCaptor<Asset> savedAsset = ArgumentCaptor.forClass(Asset.class);
    verify(assetRepository).save(savedAsset.capture());

    assertThat(savedAsset.getValue())
        .extracting(
            Asset::getPortalAssetId,
            Asset::getPortalAssetType,
            Asset::getAssetName,
            Asset::getStatus
        )
        .containsExactly(
            portalAssetId.id(),
            portalAssetType,
            assetName,
            AssetStatus.EXTANT
        );

  }

  @Test
  void getOrCreateAsset_whenHasExistingAsset_thenAssetDtoReturned() {
    var portalAssetId = new PortalAssetId("123");
    var portalAssetType = PortalAssetType.INSTALLATION;
    var assetName = "asset name";

    var asset = AssetTestUtil.builder()
        .withPortalAssetId(portalAssetId.id())
        .withPortalAssetType(portalAssetType)
        .withAssetName(assetName)
        .build();

    when(assetAccessService.getExtantAsset(portalAssetId.id(), portalAssetType))
        .thenReturn(Optional.of(asset));

    var result = assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType);

    assertThat(result)
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

    verify(assetRepository, never()).save(any());
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
            Asset::getAssetName,
            Asset::getStatus
        )
        .containsExactly(
            portalAssetId.id(),
            portalAssetType,
            assetName.value(),
            AssetStatus.EXTANT
        );
  }

  @Test
  void endAssetsWithAssetType() {
    var portalEventId = "test portal event id";
    var portalAssetType = PortalAssetType.SUBAREA;
    var firstPortalAssetId = new PortalAssetId(UUID.randomUUID().toString());
    var secondPortalAssetId = new PortalAssetId(UUID.randomUUID().toString());

    var firstAsset = spy(AssetTestUtil.builder().build());
    var secondAsset = spy(AssetTestUtil.builder().build());

    when(assetRepository.findByPortalAssetIdInAndPortalAssetType(
        List.of(firstPortalAssetId.id(), secondPortalAssetId.id()),
        portalAssetType
    )).thenReturn(List.of(firstAsset, secondAsset));

    assetPersistenceService.endAssetsWithAssetType(
        List.of(firstPortalAssetId, secondPortalAssetId),
        portalAssetType,
        portalEventId
    );

    verify(assetRepository).saveAll(List.of(firstAsset, secondAsset));

    verify(firstAsset).setStatus(AssetStatus.REMOVED);
    verify(firstAsset).setPortalEventType(PortalEventType.PEARS_TRANSACTION_OPERATION);
    verify(firstAsset).setPortalEventId(portalEventId);

    verify(secondAsset).setStatus(AssetStatus.REMOVED);
    verify(secondAsset).setPortalEventType(PortalEventType.PEARS_TRANSACTION_OPERATION);
    verify(secondAsset).setPortalEventId(portalEventId);
  }

  @Test
  void createAssetsForSubareas() {
    var licenceBlockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder().build();

    doAnswer(invocation -> invocation.getArgument(0)).when(assetRepository).saveAll(any());
    var result = assetPersistenceService.createAssetsForSubareas(List.of(licenceBlockSubareaDto));

    assertThat(result)
        .extracting(
            Asset::getPortalAssetId,
            Asset::getPortalAssetType,
            Asset::getAssetName,
            Asset::getStatus
        )
        .containsExactly(
            Tuple.tuple(
                licenceBlockSubareaDto.subareaId().id(),
                PortalAssetType.SUBAREA,
                licenceBlockSubareaDto.displayName(),
                AssetStatus.EXTANT
            )
        );
  }

}