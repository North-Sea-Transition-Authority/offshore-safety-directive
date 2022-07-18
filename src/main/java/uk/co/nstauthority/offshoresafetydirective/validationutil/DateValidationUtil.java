package uk.co.nstauthority.offshoresafetydirective.validationutil;

import java.time.DateTimeException;
import java.time.LocalDate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;

// This currently covers three field date inputs. This should be expanded to cover two field and datepicker inputs as well.
public class DateValidationUtil {

  private static final String DATE_REQUIRED_ERROR_FORMAT = "Enter a %s";
  private static final String PARTIAL_DATE_PROVIDED_ERROR_FORMAT = "Enter a complete %s";
  private static final String DATE_INVALID_ERROR_FORMAT = "%s must be a real date";
  private static final String DAY_FIELD_SUFFIX = "Day";
  private static final String MONTH_FIELD_SUFFIX = "Month";
  private static final String YEAR_FIELD_SUFFIX = "Year";
  private static final String DAY_FIELD_WITH_PREFIX_AND_SUFFIX = "%sDay%s";
  private static final String MONTH_FIELD_WITH_PREFIX_AND_SUFFIX = "%sMonth%s";
  private static final String YEAR_FIELD_WITH_PREFIX_AND_SUFFIX = "%sYear%s";

  private static final String ERROR_CODE_REQUIRED = ".required";
  private static final String ERROR_CODE_INVALID = ".invalid";
  private static final String ERROR_CODE_NOT_AFTER_TARGET_DATE = ".notAfterTargetDate";
  private static final String ERROR_CODE_NOT_BEFORE_TARGET_DATE = ".notBeforeTargetDate";

  private DateValidationUtil() {
    throw new IllegalStateException("DateValidationUtil is an util class and should not be instantiated");
  }

  /**
   * Ensures that the date is valid.
   *
   * @param fieldPrefix   The prefix of the form date fields. e.g: proposedStartDay has a prefix of proposedStart
   * @param displayPrefix The grouped name in the error message. e.g: "proposed start date"
   * @param day           Form field day
   * @param month         Form field month
   * @param year          Form field year
   * @param errors        Errors object to add rejection codes and messages to
   * @return True if the date is valid with no errors
   */
  public static boolean validateDate(String fieldPrefix,
                                     String displayPrefix,
                                     String day,
                                     String month,
                                     String year,
                                     Errors errors) {
    // validate fields are not null
    if (ObjectUtils.anyNull(day, month, year)) {
      addNullValidationErrors(
          fieldPrefix,
          day,
          month,
          year,
          displayPrefix,
          errors
      );
      return false;
    }

    // validate fields are well formed
    var dayWellFormed = isDayWellFormed(day);
    var monthWellFormed = isMonthWellFormed(month);
    var yearWellFormed = isYearWellFormed(year);

    if (!dayWellFormed || !monthWellFormed || !yearWellFormed) {
      rejectFieldsAndSetMessage(
          fieldPrefix,
          StringUtils.capitalize(String.format(DATE_INVALID_ERROR_FORMAT, displayPrefix)),
          !dayWellFormed ? String.format(DAY_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_INVALID) : null,
          !monthWellFormed ? String.format(MONTH_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_INVALID) : null,
          !yearWellFormed ? String.format(YEAR_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_INVALID) : null,
          errors
      );
      return false;
    }

    // validate date as a whole
    try {
      LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
    } catch (DateTimeException e) {
      rejectFieldsAndSetMessage(
          fieldPrefix,
          StringUtils.capitalize(String.format(DATE_INVALID_ERROR_FORMAT, displayPrefix)),
          String.format(DAY_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_INVALID),
          String.format(MONTH_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_INVALID),
          String.format(YEAR_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_INVALID),
          errors
      );
      return false;
    }

    return true;
  }

  /**
   * Ensures that the date is valid, and the date is either the current date, or is in the future.
   *
   * @param fieldPrefix   The prefix of the form date fields. e.g: proposedStartDay has a prefix of proposedStart
   * @param displayPrefix The grouped name in the error message. e.g: "proposed start date"
   * @param day           Form field day
   * @param month         Form field month
   * @param year          Form field year
   * @param errors        Errors object to add rejection codes and messages to
   * @return True if the date is valid and the date is either the current day, or is in the future
   */
  public static boolean validateDateIsTodayOrInTheFuture(String fieldPrefix,
                                                         String displayPrefix,
                                                         String day,
                                                         String month,
                                                         String year,
                                                         Errors errors) {
    return validateDateIsAfterComparisonDate(
        fieldPrefix,
        displayPrefix,
        day,
        month,
        year,
        LocalDate.now().minusDays(1),
        "today or in the future",
        errors
    );
  }

  /**
   * Ensures that the date is valid, and the date is in the future.
   *
   * @param fieldPrefix   The prefix of the form date fields. e.g: proposedStartDay has a prefix of proposedStart
   * @param displayPrefix The grouped name in the error message. e.g: "proposed start date"
   * @param day           Form field day
   * @param month         Form field month
   * @param year          Form field year
   * @param errors        Errors object to add rejection codes and messages to
   * @return True if the date is valid and the date is in the future
   */
  public static boolean validateDateIsInTheFuture(String fieldPrefix,
                                                  String displayPrefix,
                                                  String day,
                                                  String month,
                                                  String year,
                                                  Errors errors) {
    return validateDateIsAfterComparisonDate(
        fieldPrefix,
        displayPrefix,
        day,
        month,
        year,
        LocalDate.now(),
        "in the future",
        errors
    );
  }

  /**
   * Ensures that the date is valid, and the date is after the comparison date passed in.
   *
   * @param fieldPrefix    The prefix of the form date fields. e.g: proposedStartDay has a prefix of proposedStart
   * @param displayPrefix  The grouped name in the error message. e.g: "proposed start date"
   * @param day            Form field day
   * @param month          Form field month
   * @param year           Form field year
   * @param comparisonDate Date to compare with
   * @param errorSuffix    Text to append to error message
   * @param errors         Errors object to add rejection codes and messages to
   * @return True if the date is valid with no errors
   */
  public static boolean validateDateIsAfterComparisonDate(String fieldPrefix,
                                                          String displayPrefix,
                                                          String day,
                                                          String month,
                                                          String year,
                                                          LocalDate comparisonDate,
                                                          String errorSuffix,
                                                          Errors errors) {
    if (validateDate(fieldPrefix, displayPrefix, day, month, year, errors)) {
      var date = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
      if (date.isAfter(comparisonDate)) {
        return true;
      } else {
        errors.rejectValue(
            fieldPrefix + DAY_FIELD_SUFFIX,
            String.format(DAY_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_NOT_AFTER_TARGET_DATE),
            String.format("%s must be %s", StringUtils.capitalize(displayPrefix), errorSuffix)
        );
        errors.rejectValue(
            fieldPrefix + MONTH_FIELD_SUFFIX,
            String.format(MONTH_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_NOT_AFTER_TARGET_DATE),
            ""
        );
        errors.rejectValue(
            fieldPrefix + YEAR_FIELD_SUFFIX,
            String.format(YEAR_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_NOT_AFTER_TARGET_DATE),
            ""
        );
        return false;
      }
    }

    return false;
  }


  /**
   * Ensures that the date is valid, and the date is either the current date, or is in the past.
   *
   * @param fieldPrefix   The prefix of the form date fields. e.g: proposedStartDay has a prefix of proposedStart
   * @param displayPrefix The grouped name in the error message. e.g: "proposed start date"
   * @param day           Form field day
   * @param month         Form field month
   * @param year          Form field year
   * @param errors        Errors object to add rejection codes and messages to
   * @return True if the date is valid and the date is either the current day, or is in the past
   */
  public static boolean validateDateIsTodayOrInThePast(String fieldPrefix,
                                                       String displayPrefix,
                                                       String day,
                                                       String month,
                                                       String year,
                                                       Errors errors) {
    return validateDateIsBeforeComparisonDate(
        fieldPrefix,
        displayPrefix,
        day,
        month,
        year,
        LocalDate.now().plusDays(1),
        "today or in the past",
        errors
    );
  }

  /**
   * Ensures that the date is valid, and the date is in the past.
   *
   * @param fieldPrefix   The prefix of the form date fields. e.g: proposedStartDay has a prefix of proposedStart
   * @param displayPrefix The grouped name in the error message. e.g: "proposed start date"
   * @param day           Form field day
   * @param month         Form field month
   * @param year          Form field year
   * @param errors        Errors object to add rejection codes and messages to
   * @return True if the date is valid and the date in the past
   */
  public static boolean validateDateIsInThePast(String fieldPrefix,
                                                String displayPrefix,
                                                String day,
                                                String month,
                                                String year,
                                                Errors errors) {
    return validateDateIsBeforeComparisonDate(
        fieldPrefix,
        displayPrefix,
        day,
        month,
        year,
        LocalDate.now(),
        "in the past",
        errors
    );
  }

  /**
   * Ensures that the date is valid, and the date is before the comparison date passed in.
   *
   * @param fieldPrefix    The prefix of the form date fields. e.g: proposedStartDay has a prefix of proposedStart
   * @param displayPrefix  The grouped name in the error message. e.g: "proposed start date"
   * @param day            Form field day
   * @param month          Form field month
   * @param year           Form field year
   * @param comparisonDate Date to compare with
   * @param errorSuffix    Text to append to error message
   * @param errors         Errors object to add rejection codes and messages to
   * @return True if the date is valid with no errors
   */
  public static boolean validateDateIsBeforeComparisonDate(String fieldPrefix,
                                                           String displayPrefix,
                                                           String day,
                                                           String month,
                                                           String year,
                                                           LocalDate comparisonDate,
                                                           String errorSuffix,
                                                           Errors errors) {
    if (validateDate(fieldPrefix, displayPrefix, day, month, year, errors)) {
      var date = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
      if (date.isBefore(comparisonDate)) {
        return true;
      } else {
        errors.rejectValue(
            fieldPrefix + DAY_FIELD_SUFFIX,
            String.format(DAY_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_NOT_BEFORE_TARGET_DATE),
            String.format("%s must be %s", StringUtils.capitalize(displayPrefix), errorSuffix)
        );
        errors.rejectValue(
            fieldPrefix + MONTH_FIELD_SUFFIX,
            String.format(MONTH_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_NOT_BEFORE_TARGET_DATE),
            ""
        );
        errors.rejectValue(
            fieldPrefix + YEAR_FIELD_SUFFIX,
            String.format(YEAR_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_NOT_BEFORE_TARGET_DATE),
            ""
        );
        return false;
      }
    }
    return false;
  }

  /**
   * Rejects the day/month/year fields with the given codes and adds the error message to the 'first' rejected field for
   * compatibility with FDS.
   * A null code indicates that field should not be rejected.
   * At least one of the day, month or year codes must be not null.
   *
   * @param fieldPrefix    The prefix of the form date fields. e.g: proposedStartDay has a prefix of proposedStart
   * @param errorMessage   The error message to attach to the first field with an error
   * @param dayErrorCode   An error code string to attach to the day field. This should be null if the day field has no error
   * @param monthErrorCode An error code string to attach to the month field. This should be null if the month field has no error
   * @param yearErrorCode  An error code string to attach to the year field. This should be null if the year field has no error
   * @param errors         THe Errors object to add rejection codes and messages to
   */
  private static void rejectFieldsAndSetMessage(String fieldPrefix,
                                                String errorMessage,
                                                String dayErrorCode,
                                                String monthErrorCode,
                                                String yearErrorCode,
                                                Errors errors) {
    if (ObjectUtils.allNull(dayErrorCode, monthErrorCode, yearErrorCode)) {
      throw new IllegalArgumentException("At least one of day, month or year codes must be provided.");
    }

    if (dayErrorCode != null) {
      errors.rejectValue(
          fieldPrefix + DAY_FIELD_SUFFIX,
          dayErrorCode,
          errorMessage
      );
    }

    if (monthErrorCode != null) {
      if (dayErrorCode == null) {
        errors.rejectValue(
            fieldPrefix + MONTH_FIELD_SUFFIX,
            monthErrorCode,
            errorMessage
        );
      } else {
        errors.rejectValue(
            fieldPrefix + MONTH_FIELD_SUFFIX,
            monthErrorCode,
            ""
        );
      }
    }

    if (yearErrorCode != null) {
      if (dayErrorCode == null && monthErrorCode == null) {
        errors.rejectValue(
            fieldPrefix + YEAR_FIELD_SUFFIX,
            yearErrorCode,
            errorMessage
        );
      } else {
        errors.rejectValue(
            fieldPrefix + YEAR_FIELD_SUFFIX,
            yearErrorCode,
            ""
        );
      }
    }
  }

  private static boolean isDayWellFormed(String dayString) {
    try {
      int day = Integer.parseInt(dayString);
      return Range.between(1, 31).contains(day);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private static boolean isMonthWellFormed(String monthString) {
    try {
      int month = Integer.parseInt(monthString);
      return Range.between(1, 12).contains(month);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private static boolean isYearWellFormed(String yearString) {
    try {
      int year = Integer.parseInt(yearString);
      return Range.between(1, 5000).contains(year);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private static void addNullValidationErrors(String fieldPrefix,
                                              String day,
                                              String month,
                                              String year,
                                              String displayPrefix,
                                              Errors errors) {
    rejectFieldsAndSetMessage(
        fieldPrefix,
        ObjectUtils.allNull(day, month, year) ? String.format(DATE_REQUIRED_ERROR_FORMAT,
            displayPrefix) : String.format(PARTIAL_DATE_PROVIDED_ERROR_FORMAT, displayPrefix),
        day == null ? String.format(DAY_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_REQUIRED) : null,
        month == null ? String.format(MONTH_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_REQUIRED) : null,
        year == null ? String.format(YEAR_FIELD_WITH_PREFIX_AND_SUFFIX, fieldPrefix, ERROR_CODE_REQUIRED) : null,
        errors
    );
  }
}