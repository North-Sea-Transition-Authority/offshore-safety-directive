package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.time.Instant;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;

public record AppointmentCorrectionHistoryView(
    UUID correctionId,
    AppointmentId appointmentId,
    Instant createdInstant,
    String createdBy,
    String reason
) {

  public static AppointmentCorrectionHistoryView fromAppointmentCorrection(AppointmentCorrection correction,
                                                                           String user) {
    return new AppointmentCorrectionHistoryView(
        correction.getId(),
        new AppointmentId(correction.getAppointment().getId()),
        correction.getCreatedTimestamp(),
        user,
        correction.getReasonForCorrection()
    );
  }

  public String formattedCreatedDatetime() {
    return DateUtil.formatLongDateTime(this.createdInstant());
  }

}
