package uk.co.nstauthority.offshoresafetydirective.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class DateUtil {

  static final DateTimeFormatter LONG_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm");
  static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy");
  static final DateTimeFormatter LONG_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");

  private DateUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  /**
   * Produce a GDS-complaint long date time string for the provided date and time.
   *
   * @param temporal The temporal to format
   * @return The formatted temporal in a GDS-complaint long date format
   */
  public static String formatLongDateTime(Temporal temporal) {
    return format(temporal, LONG_DATE_TIME_FORMATTER);
  }

  /**
   * Produce a GDS-complaint short date string for the provided date.
   *
   * @param temporal The LocalDate to format
   * @return The formatted temporal in a GDS-complaint short date format
   */
  public static String formatShortDate(Temporal temporal) {
    return format(temporal, SHORT_DATE_FORMATTER);
  }

  /**
   * Produce a GDS-complaint date string with full month format for the provided date.
   *
   * @param temporal The date to format
   * @return The formatted temporal in a GDS-complaint long date format
   */
  public static String formatLongDate(Temporal temporal) {
    return format(temporal, LONG_DATE_FORMATTER);
  }

  private static String format(Temporal temporal, DateTimeFormatter dateTimeFormatter) {

    if (temporal instanceof Instant instant) {
      temporal = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    return dateTimeFormatter.format(temporal);
  }

}
