package uk.co.nstauthority.offshoresafetydirective.date;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class DateUtilTest {

  @Test
  void formatDateTime_whenInstant_thenExpectFormattedDate() {
    var timeToFormat = LocalDateTime.of(2022, Month.JANUARY, 16, 17, 5, 48).toInstant(ZoneOffset.UTC);
    var result = DateUtil.formatDateTime(timeToFormat);
    assertThat(result).isEqualTo("16 Jan 2022 17:05");
  }

  @Test
  void formatDateTime_whenLocalDateTime_thenExpectFormattedDate() {
    var timeToFormat = LocalDateTime.of(2022, Month.JANUARY, 16, 17, 5, 48);
    var result = DateUtil.formatDateTime(timeToFormat);
    assertThat(result).isEqualTo("16 Jan 2022 17:05");
  }

  @Test
  void formatDate_whenLocalDate_thenExpectFormattedDate() {
    var timeToFormat = LocalDate.of(2022, Month.JANUARY, 16);
    var result = DateUtil.formatDate(timeToFormat);
    assertThat(result).isEqualTo("16 Jan 2022");
  }

  @Test
  void formatDateTime_whenInstant_andDayIsSingleDigit_thenExpectFormattedDateWithSingleDigitDay() {
    var timeToFormat = LocalDateTime.of(2022, Month.JANUARY, 1, 17, 5, 48).toInstant(ZoneOffset.UTC);
    var result = DateUtil.formatDateTime(timeToFormat);
    assertThat(result).isEqualTo("1 Jan 2022 17:05");
  }

  @Test
  void formatDateTime_whenLocalDateTime_andDayIsSingleDigit_thenExpectFormattedDateWithSingleDigitDay() {
    var timeToFormat = LocalDateTime.of(2022, Month.JANUARY, 1, 17, 5, 48);
    var result = DateUtil.formatDateTime(timeToFormat);
    assertThat(result).isEqualTo("1 Jan 2022 17:05");
  }

  @Test
  void formatDate_whenLocalDate_andDayIsSingleDigit_thenExpectFormattedDateWithSingleDigitDay() {
    var timeToFormat = LocalDate.of(2022, Month.JANUARY, 1);
    var result = DateUtil.formatDate(timeToFormat);
    assertThat(result).isEqualTo("1 Jan 2022");
  }


  @Test
  void formatLongDate_whenInstant_thenExpectFormattedDate() {
    var timeToFormat = LocalDateTime.of(2022, Month.JANUARY, 16, 17, 5, 48).toInstant(ZoneOffset.UTC);
    var result = DateUtil.formatLongDate(timeToFormat);
    assertThat(result).isEqualTo("16 January 2022");
  }

  @Test
  void formatLongDate_whenInstant_andDayIsSingleDigit_thenExpectFormattedDateWithSingleDigitDay() {
    var timeToFormat = LocalDateTime.of(2022, Month.JANUARY, 1, 17, 5, 48).toInstant(ZoneOffset.UTC);
    var result = DateUtil.formatLongDate(timeToFormat);
    assertThat(result).isEqualTo("1 January 2022");
  }

}