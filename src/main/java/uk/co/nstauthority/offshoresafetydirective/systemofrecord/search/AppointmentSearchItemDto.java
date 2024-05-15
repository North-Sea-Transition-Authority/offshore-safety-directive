package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.time.LocalDate;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;

public record AppointmentSearchItemDto(
    PortalAssetId assetId,
    AssetName assetName,
    AppointedOperatorName appointedOperatorName,
    LocalDate appointmentDate,
    AppointmentType appointmentType,
    String timelineUrl
) {

  public String displayableAppointmentDate() {
    if (appointmentDate != null) {
      return DateUtil.formatLongDate(appointmentDate);
    } else {
      return "";
    }
  }

}
