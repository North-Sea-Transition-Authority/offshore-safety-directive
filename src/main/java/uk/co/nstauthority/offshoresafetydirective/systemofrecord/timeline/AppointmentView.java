package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedPortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;

public record AppointmentView(
    AppointmentId appointmentId,
    AppointedPortalAssetId portalAssetId,
    String appointedOperatorName,
    AppointmentFromDate appointmentFromDate,
    AppointmentToDate appointmentToDate
) {
}
