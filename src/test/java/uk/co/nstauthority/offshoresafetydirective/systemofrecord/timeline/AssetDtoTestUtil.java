package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

public class AssetDtoTestUtil {

  private AssetDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private AssetId assetId = new AssetId(UUID.randomUUID());

    private PortalAssetId portalAssetId = new PortalAssetId("portal asset id");

    private AssetName assetName = new AssetName("asset name");

    private PortalAssetType portalAssetType = PortalAssetType.INSTALLATION;

    public Builder withAssetId(UUID assetId) {
      this.assetId = new AssetId(assetId);
      return this;
    }

    public Builder withPortalAssetId(String portalAssetId) {
      this.portalAssetId = new PortalAssetId(portalAssetId);
      return this;
    }

    public Builder withAssetName(String assetName) {
      this.assetName = new AssetName(assetName);
      return this;
    }

    public Builder withPortalAssetType(PortalAssetType portalAssetType) {
      this.portalAssetType = portalAssetType;
      return this;
    }

    public AssetDto build() {
      return new AssetDto(
          assetId,
          portalAssetId,
          assetName,
          portalAssetType
      );
    }
  }
}
