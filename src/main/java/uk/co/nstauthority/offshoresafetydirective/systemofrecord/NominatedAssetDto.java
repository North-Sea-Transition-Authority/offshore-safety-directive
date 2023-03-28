package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.List;

record NominatedAssetDto(
    PortalAssetId portalAssetId,
    PortalAssetType portalAssetType,
    List<String> phases
) {
}
