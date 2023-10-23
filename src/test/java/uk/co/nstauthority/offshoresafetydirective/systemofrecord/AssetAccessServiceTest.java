package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

  @Test
  void isAssetExtant_whenAssetExists_andIsExtant_thenTrue() {
    var portalAssetId = new PortalAssetId("123");
    var portalAssetType = PortalAssetType.WELLBORE;

    var asset = AssetTestUtil.builder()
        .withPortalAssetId(portalAssetId.id())
        .withAssetStatus(AssetStatus.EXTANT)
        .withPortalAssetType(portalAssetType)
        .build();

    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(portalAssetId.id(), portalAssetType, AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var resultingIsAssetExtant = assetAccessService.isAssetExtant(portalAssetId, portalAssetType);

    assertTrue(resultingIsAssetExtant);
  }

  @ParameterizedTest
  @EnumSource(value = AssetStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "EXTANT")
  void isAssetExtant_whenAssetExists_andIsNotExtant_thenFalse(AssetStatus nonExtantStatus) {
    var portalAssetId = new PortalAssetId("123");
    var portalAssetType = PortalAssetType.WELLBORE;

    var asset = AssetTestUtil.builder()
        .withPortalAssetId(portalAssetId.id())
        .withAssetStatus(nonExtantStatus)
        .withPortalAssetType(portalAssetType)
        .build();

    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(portalAssetId.id(), portalAssetType, AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var resultingIsAssetExtant = assetAccessService.isAssetExtant(portalAssetId, portalAssetType);

    assertFalse(resultingIsAssetExtant);
  }

  @Test
  void isAssetExtant_whenAssetDoesntExists_thenReturnFalse() {
    var portalAssetId = new PortalAssetId("123");
    var portalAssetType = PortalAssetType.WELLBORE;

    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(portalAssetId.id(), portalAssetType, AssetStatus.EXTANT))
        .thenReturn(Optional.empty());

    assertFalse(() -> assetAccessService.isAssetExtant(portalAssetId, portalAssetType));
  }
}