package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

class AssetDtoTestUtil {

  private AssetDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private AssetId assetId = new AssetId(UUID.randomUUID());

    private PortalAssetId portalAssetId = new PortalAssetId("portal asset id");

    private AssetName assetName = new AssetName("asset name");

    private PortalAssetType portalAssetType = PortalAssetType.INSTALLATION;

    Builder withAssetId(UUID assetId) {
      this.assetId = new AssetId(assetId);
      return this;
    }

    Builder withPortalAssetId(String portalAssetId) {
      this.portalAssetId = new PortalAssetId(portalAssetId);
      return this;
    }

    Builder withAssetName(String assetName) {
      this.assetName = new AssetName(assetName);
      return this;
    }

    Builder withPortalAssetType(PortalAssetType portalAssetType) {
      this.portalAssetType = portalAssetType;
      return this;
    }

    AssetDto build() {
      return new AssetDto(
          assetId,
          portalAssetId,
          assetName,
          portalAssetType
      );
    }
  }
}
