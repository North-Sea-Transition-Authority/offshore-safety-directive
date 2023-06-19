package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;

public record AppointmentView(
    AppointmentId appointmentId,
    String appointedOperatorName,
    AppointmentFromDate appointmentFromDate,
    AppointmentToDate appointmentToDate,
    List<AssetAppointmentPhase> phases,
    String createdByReference,
    String nominationUrl,
    String updateUrl,
    AssetDto assetDto
) {
}
