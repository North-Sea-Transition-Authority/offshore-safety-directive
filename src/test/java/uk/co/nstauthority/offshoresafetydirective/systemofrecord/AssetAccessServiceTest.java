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

    var expectedAsset = AssetTestUtil.builder().build();

    given(assetRepository.findByPortalAssetId(matchedPortalAssetId.id()))
        .willReturn(Optional.of(expectedAsset));

    var resultingAssetDto = assetAccessService.getAsset(matchedPortalAssetId);

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

    var unmatchedPortalAssetId = new PortalAssetId("not from portal asset id");

    given(assetRepository.findByPortalAssetId(unmatchedPortalAssetId.id()))
        .willReturn(Optional.empty());

    var resultingAssetDto = assetAccessService.getAsset(unmatchedPortalAssetId);

    assertThat(resultingAssetDto).isEmpty();
  }
}