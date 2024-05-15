package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class AssetTestUtil {

  private AssetTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private String portalAssetId = UUID.randomUUID().toString();
    private PortalAssetType portalAssetType = PortalAssetType.INSTALLATION;
    private String assetName = "asset name";
    private AssetStatus assetStatus = AssetStatus.EXTANT;

    private Builder() {
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withPortalAssetId(String portalAssetId) {
      this.portalAssetId = portalAssetId;
      return this;
    }

    public Builder withPortalAssetType(PortalAssetType portalAssetType) {
      this.portalAssetType = portalAssetType;
      return this;
    }

    public Builder withAssetName(String assetName) {
      this.assetName = assetName;
      return this;
    }

    public Builder withAssetStatus(AssetStatus assetStatus) {
      this.assetStatus = assetStatus;
      return this;
    }

    public Asset build() {
      var asset = new Asset(id);
      asset.setPortalAssetId(portalAssetId);
      asset.setPortalAssetType(portalAssetType);
      asset.setAssetName(assetName);
      asset.setStatus(assetStatus);
      return asset;
    }

  }
}