package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

public record AppointmentCorrectionValidationHint(
    AppointmentId appointmentId,
    AssetId assetId,
    PortalAssetType portalAssetType
) {
}
