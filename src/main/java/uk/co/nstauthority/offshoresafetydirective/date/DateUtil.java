package uk.co.nstauthority.offshoresafetydirective.date;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class DateUtil {

  static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm");
  static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy");

  private DateUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  /**
   * Produce a GDS-complaint string for the provided date and time.
   *
   * @param localDateTime The LocalDateTime to format
   * @return The formatted temporal in a GDS-complaint format
   */
  public static String formatDateTime(LocalDateTime localDateTime) {
    return DATE_TIME_FORMATTER.format(localDateTime);
  }

  /**
   * Produce a GDS-complaint string for the provided date and time.
   *
   * @param instant The Instant to format
   * @return The formatted temporal in a GDS-complaint format
   */
  public static String formatDateTime(Instant instant) {
    var localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    return formatDateTime(localDateTime);
  }

  /**
   * Produce a GDS-complaint string for the provided date.
   *
   * @param localDate The LocalDate to format
   * @return The formatted temporal in a GDS-complaint format
   */
  public static String formatDate(LocalDate localDate) {
    return DATE_FORMATTER.format(localDate);
  }

}
