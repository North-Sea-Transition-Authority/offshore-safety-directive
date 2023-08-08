package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;

public record NominatedSubareaWellDetailView(WellboreId wellboreId, AssetName assetName) {
}
