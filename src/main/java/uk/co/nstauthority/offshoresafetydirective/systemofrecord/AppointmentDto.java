package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Instant;

public record AppointmentDto(
    AppointmentId appointmentId,
    AppointedPortalAssetId portalAssetId,
    AppointedOperatorId appointedOperatorId,
    AppointmentFromDate appointmentFromDate,
    AppointmentToDate appointmentToDate,
    Instant appointmentCreatedDate
) {

  static AppointmentDto fromAppointment(Appointment appointment) {
    return new AppointmentDto(
        new AppointmentId(appointment.getId()),
        new AppointedPortalAssetId(appointment.getAsset().getPortalAssetId()),
        new AppointedOperatorId(String.valueOf(appointment.getAppointedPortalOperatorId())),
        new AppointmentFromDate(appointment.getResponsibleFromDate()),
        new AppointmentToDate(appointment.getResponsibleToDate()),
        appointment.getCreatedDatetime()
    );
  }
}
