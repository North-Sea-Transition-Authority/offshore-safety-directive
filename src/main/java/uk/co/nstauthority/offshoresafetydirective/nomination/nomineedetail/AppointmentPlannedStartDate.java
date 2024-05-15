package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record AppointmentPlannedStartDate(LocalDate plannedStartDate) {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");

  public String plannedStartDateString() {
    return DATE_TIME_FORMATTER.format(plannedStartDate);
  }

}
