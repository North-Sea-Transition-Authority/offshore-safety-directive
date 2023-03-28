package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedPortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;

public record AppointmentView(
    AppointmentId appointmentId,
    AppointedPortalAssetId portalAssetId,
    String appointedOperatorName,
    AppointmentFromDate appointmentFromDate,
    AppointmentToDate appointmentToDate,
    List<AssetAppointmentPhase> phases,
    String createdByReference,
    String nominationUrl
) {
}
