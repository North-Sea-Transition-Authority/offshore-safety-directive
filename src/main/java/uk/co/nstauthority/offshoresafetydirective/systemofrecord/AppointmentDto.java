package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Instant;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

public record AppointmentDto(
    AppointmentId appointmentId,
    AppointedPortalAssetId portalAssetId,
    AppointedOperatorId appointedOperatorId,
    AppointmentFromDate appointmentFromDate,
    AppointmentToDate appointmentToDate,
    Instant appointmentCreatedDate,
    AppointmentType appointmentType,
    String legacyNominationReference,
    NominationId nominationId
) {

  static AppointmentDto fromAppointment(Appointment appointment) {
    return new AppointmentDto(
        new AppointmentId(appointment.getId()),
        new AppointedPortalAssetId(appointment.getAsset().getPortalAssetId()),
        new AppointedOperatorId(String.valueOf(appointment.getAppointedPortalOperatorId())),
        new AppointmentFromDate(appointment.getResponsibleFromDate()),
        new AppointmentToDate(appointment.getResponsibleToDate()),
        appointment.getCreatedDatetime(),
        appointment.getAppointmentType(),
        appointment.getCreatedByLegacyNominationReference(),
        (appointment.getCreatedByNominationId() != null)
            ? new NominationId(appointment.getCreatedByNominationId())
            : null
    );
  }
}
