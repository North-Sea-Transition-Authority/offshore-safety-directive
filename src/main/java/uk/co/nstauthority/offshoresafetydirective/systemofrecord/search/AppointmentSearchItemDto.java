package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.time.LocalDate;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;

public record AppointmentSearchItemDto(
    AppointedPortalAssetId assetId,
    AssetName assetName,
    AppointedOperatorName appointedOperatorName,
    LocalDate appointmentDate,
    AppointmentType appointmentType
) {

  public String displayableAppointmentDate() {
    return DateUtil.formatLongDate(appointmentDate);
  }

}
