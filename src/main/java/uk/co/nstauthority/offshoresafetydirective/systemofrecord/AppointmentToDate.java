package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.LocalDate;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;

public record AppointmentToDate(LocalDate value) {

  public String formattedValue() {
    return (value != null)
        ? DateUtil.formatLongDate(value)
        : "";
  }
}
