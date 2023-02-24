package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailViewTestUtil;

@ExtendWith(MockitoExtension.class)
class AssetPersistenceServiceTest {

  @Mock
  private AssetRepository assetRepository;

  @InjectMocks
  private AssetPersistenceService assetPersistenceService;

  @Test
  void getExistingOrCreateAssets_whenNoneExisting_verifySaved() {
    var installationDto = InstallationDtoTestUtil.builder().build();
    var installationDetailView = NominatedInstallationDetailViewTestUtil.builder()
        .withInstallations(List.of(installationDto))
        .build();

    // Return the arguments passed into the call
    when(assetRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var assets = assetPersistenceService.getExistingOrCreateAssets(installationDetailView);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Asset>> assetListCaptor = ArgumentCaptor.forClass(List.class);
    verify(assetRepository).saveAll(assetListCaptor.capture());

    assertThat(assetListCaptor.getValue())
        .extracting(Asset::getPortalAssetId)
        .containsExactly(String.valueOf(installationDto.id()));

    assertThat(assets)
        .extracting(
            Asset::getPortalAssetId,
            Asset::getPortalAssetType
        )
        .containsExactly(
            Tuple.tuple(String.valueOf(installationDto.id()), PortalAssetType.INSTALLATION)
        );
  }

  @Test
  void getExistingOrCreateAssets_whenAllExisting_verifyNoneSaved() {
    var installationDto = InstallationDtoTestUtil.builder().build();
    var installationDetailView = NominatedInstallationDetailViewTestUtil.builder()
        .withInstallations(List.of(installationDto))
        .build();
    var existingAsset = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(installationDto.id()))
        .build();

    when(assetRepository.findAllByPortalAssetIdIn(List.of(String.valueOf(installationDto.id()))))
        .thenReturn(List.of(existingAsset));

    var assets = assetPersistenceService.getExistingOrCreateAssets(installationDetailView);

    verify(assetRepository, never()).saveAll(any());

    assertThat(assets).containsExactly(existingAsset);
  }

  @Test
  void getExistingOrCreateAssets_whenSomeExisting_verifyOnlyNonExistingSaved() {
    var existingInstallationDto = InstallationDtoTestUtil.builder()
        .withId(100)
        .build();
    var nonExistingInstallationDto = InstallationDtoTestUtil.builder()
        .withId(200)
        .build();
    var installationDetailView = NominatedInstallationDetailViewTestUtil.builder()
        .withInstallations(List.of(existingInstallationDto, nonExistingInstallationDto))
        .build();
    var existingAsset = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(existingInstallationDto.id()))
        .build();

    when(assetRepository.findAllByPortalAssetIdIn(List.of(
        String.valueOf(existingInstallationDto.id()),
        String.valueOf(nonExistingInstallationDto.id())
    )))
        .thenReturn(List.of(existingAsset));

    // Return the arguments passed into the call
    when(assetRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var assets = assetPersistenceService.getExistingOrCreateAssets(installationDetailView);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Asset>> assetListCaptor = ArgumentCaptor.forClass(List.class);
    verify(assetRepository).saveAll(assetListCaptor.capture());

    assertThat(assetListCaptor.getValue())
        .extracting(Asset::getPortalAssetId)
        .containsExactly(String.valueOf(nonExistingInstallationDto.id()));

    assertThat(assets)
        .extracting(
            Asset::getPortalAssetId,
            Asset::getPortalAssetType
        )
        .containsExactly(
            Tuple.tuple(String.valueOf(existingInstallationDto.id()), PortalAssetType.INSTALLATION),
            Tuple.tuple(String.valueOf(nonExistingInstallationDto.id()), PortalAssetType.INSTALLATION)
        );
  }
}