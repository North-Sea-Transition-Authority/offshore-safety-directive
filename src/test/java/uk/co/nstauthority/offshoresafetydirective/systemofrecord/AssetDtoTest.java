package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AssetDtoTest {

  @Test
  void fromAsset_verifyMappings() {

    var assetId = UUID.randomUUID();

    var asset = AssetTestUtil.builder()
        .withId(assetId)
        .withAssetName("asset name")
        .withPortalAssetId("portal asset id")
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withAssetStatus(AssetStatus.EXTANT)
        .build();

    var resultingAssetDto = AssetDto.fromAsset(asset);

    assertThat(resultingAssetDto)
        .extracting(
            AssetDto::assetId,
            AssetDto::assetName,
            AssetDto::portalAssetId,
            AssetDto::portalAssetType,
            AssetDto::status
        )
        .containsExactly(
            new AssetId(assetId),
            new AssetName("asset name"),
            new PortalAssetId("portal asset id"),
            PortalAssetType.INSTALLATION,
            AssetStatus.EXTANT
        );
  }

}