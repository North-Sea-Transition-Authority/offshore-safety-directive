package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.List;

public record NominatedAssetDto(
    PortalAssetId portalAssetId,
    PortalAssetType portalAssetType,
    AssetName portalAssetName,
    Collection<String> phases
) {

  public NominatedAssetDto(PortalAssetId portalAssetId, PortalAssetType portalAssetType, AssetName portalAssetName) {
    this(portalAssetId, portalAssetType, portalAssetName, List.of());
  }
}
