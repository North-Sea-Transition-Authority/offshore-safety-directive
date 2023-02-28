package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.time.LocalDate;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedPortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;

public record AppointmentSearchItemDto(
    AppointedPortalAssetId assetId,
    AssetName assetName,
    AppointedOperatorName appointedOperatorName,
    LocalDate appointmentDate,
    AppointmentType appointmentType,
    String timelineUrl
) {

  public String displayableAppointmentDate() {
    return DateUtil.formatLongDate(appointmentDate);
  }

}
