package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;

record AppointmentCorrectionValidationHint(
    AppointmentDto appointmentDto,
    AssetDto assetDto
) {
}
