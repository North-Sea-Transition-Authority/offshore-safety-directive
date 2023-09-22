package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssetAccessServiceTest {

  @Mock
  private AssetRepository assetRepository;

  @InjectMocks
  private AssetAccessService assetAccessService;

  @Test
  void getAsset_whenFound_thenPopulatedOptional() {

    var matchedPortalAssetId = new PortalAssetId("portal asset id");

    var portalAssetType = PortalAssetType.INSTALLATION;
    var expectedAsset = AssetTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    given(assetRepository.findByPortalAssetIdAndPortalAssetType(matchedPortalAssetId.id(), portalAssetType))
        .willReturn(Optional.of(expectedAsset));

    var resultingAssetDto = assetAccessService.getAsset(matchedPortalAssetId, portalAssetType);

    assertThat(resultingAssetDto).isPresent();
    assertThat(resultingAssetDto.get())
        .extracting(
            AssetDto::assetId,
            AssetDto::assetName,
            AssetDto::portalAssetId,
            AssetDto::portalAssetType
        )
        .contains(
            new AssetId(expectedAsset.getId()),
            new AssetName(expectedAsset.getAssetName()),
            new PortalAssetId(expectedAsset.getPortalAssetId()),
            expectedAsset.getPortalAssetType()
        );
  }

  @Test
  void getAsset_whenNotFound_thenEmptyOptional() {

    var portalAssetType = PortalAssetType.INSTALLATION;
    var unmatchedPortalAssetId = new PortalAssetId("not from portal asset id");

    given(assetRepository.findByPortalAssetIdAndPortalAssetType(unmatchedPortalAssetId.id(), portalAssetType))
        .willReturn(Optional.empty());

    var resultingAssetDto = assetAccessService.getAsset(unmatchedPortalAssetId, portalAssetType);

    assertThat(resultingAssetDto).isEmpty();
  }
}