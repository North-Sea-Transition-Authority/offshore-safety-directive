package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record AppointmentPlannedStartDate(String plannedStartDate) {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");

  public static AppointmentPlannedStartDate fromDate(LocalDate plannedStartDate) {
    return new AppointmentPlannedStartDate(DATE_TIME_FORMATTER.format(plannedStartDate));
  }

}
