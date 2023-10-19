package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

public record AssetDto(
    AssetId assetId,
    PortalAssetId portalAssetId,
    AssetName assetName,
    PortalAssetType portalAssetType,
    AssetStatus status
) {

  public static AssetDto fromAsset(Asset asset) {
    return new AssetDto(
        new AssetId(asset.getId()),
        new PortalAssetId(asset.getPortalAssetId()),
        new AssetName(asset.getAssetName()),
        asset.getPortalAssetType(),
        asset.getStatus()
    );
  }
}
