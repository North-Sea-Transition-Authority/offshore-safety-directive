package uk.co.nstauthority.offshoresafetydirective.date;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateUtilTest {

  @ParameterizedTest
  @MethodSource("getMiddleOfMonthTestDateTimes")
  void formatDateTime_thenExpectFormattedDate(Temporal temporalToFormat) {
    var result = DateUtil.formatLongDateTime(temporalToFormat);
    assertThat(result).isEqualTo("16 January 2022 17:05");
  }

  @ParameterizedTest
  @MethodSource("getStartOfMonthTestDateTimes")
  void formatDateTime_whenDayIsSingleDigit_thenExpectFormattedDateWithSingleDigitDay(Temporal temporalToFormat) {
    var result = DateUtil.formatLongDateTime(temporalToFormat);
    assertThat(result).isEqualTo("1 January 2022 17:05");
  }

  @ParameterizedTest
  @MethodSource("getMiddleOfMonthTestDates")
  void formatShortDate_thenExpectFormattedDate(Temporal temporalToFormat) {
    var result = DateUtil.formatShortDate(temporalToFormat);
    assertThat(result).isEqualTo("16 Jan 2022");
  }

  @ParameterizedTest
  @MethodSource("getStartOfMonthTestDates")
  void formatShortDate_whenDayIsSingleDigit_thenExpectFormattedDateWithSingleDigitDay(Temporal temporalToFormat) {
    var result = DateUtil.formatShortDate(temporalToFormat);
    assertThat(result).isEqualTo("1 Jan 2022");
  }


  @ParameterizedTest
  @MethodSource("getMiddleOfMonthTestDates")
  void formatLongDate_thenExpectFormattedDate(Temporal temporalToFormat) {
    var result = DateUtil.formatLongDate(temporalToFormat);
    assertThat(result).isEqualTo("16 January 2022");
  }

  @ParameterizedTest
  @MethodSource("getStartOfMonthTestDates")
  void formatLongDate_whenDayIsSingleDigit_thenExpectFormattedDateWithSingleDigitDay(Temporal temporalToFormat) {
    var result = DateUtil.formatLongDate(temporalToFormat);
    assertThat(result).isEqualTo("1 January 2022");
  }

  private static Stream<Arguments> getMiddleOfMonthTestDates() {

    // instant and local date of the same date
    return Stream.of(
        Arguments.of(LocalDateTime.of(2022, Month.JANUARY, 16, 17, 5, 48).toInstant(ZoneOffset.UTC)),
        Arguments.of(LocalDate.of(2022, Month.JANUARY, 16))
    );
  }

  private static Stream<Arguments> getMiddleOfMonthTestDateTimes() {

    // instant and local date time of the same date time
    return Stream.of(
        Arguments.of(LocalDateTime.of(2022, Month.JANUARY, 16, 17, 5, 48).toInstant(ZoneOffset.UTC)),
        Arguments.of(LocalDateTime.of(2022, Month.JANUARY, 16, 17, 5, 48))
    );
  }

  private static Stream<Arguments> getStartOfMonthTestDates() {
    // instant and local date of the same date
    return Stream.of(
        Arguments.of(LocalDateTime.of(2022, Month.JANUARY, 1, 17, 5, 48).toInstant(ZoneOffset.UTC)),
        Arguments.of(LocalDate.of(2022, Month.JANUARY, 1))
    );
  }

  private static Stream<Arguments> getStartOfMonthTestDateTimes() {
    // instant and local date time of the same date time
    return Stream.of(
        Arguments.of(LocalDateTime.of(2022, Month.JANUARY, 1, 17, 5, 48).toInstant(ZoneOffset.UTC)),
        Arguments.of(LocalDateTime.of(2022, Month.JANUARY, 1, 17, 5, 48))
    );
  }

}