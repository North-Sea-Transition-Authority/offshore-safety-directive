package uk.co.nstauthority.offshoresafetydirective.validationutil;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

class DateValidationUtilTest {

  private Errors errors;

  @BeforeEach
  void setUp() {
    errors = new BeanPropertyBindingResult(new DateValidationTestForm(null,null,null), "form");
  }

  @Test
  void validateDate_allNull() {
    var isValid = invokeValidateDate(null, null, null, errors);

    assertCodes(
        errors,
        "dateOfBirthDay.required",
        "dateOfBirthMonth.required",
        "dateOfBirthYear.required"
    );
    assertMessages(
        errors,
        "Enter a date of birth",
        "",
        ""
    );
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate() {
    var isValid = invokeValidateDate("01", "12", "2020", errors);

    assertThat(errors.getAllErrors()).isEmpty();
    assertThat(isValid).isTrue();
  }

  @Test
  void validateDate_dayNull() {
    var isValid = invokeValidateDate(null, "12", "2020", errors);

    assertCodes(errors, "dateOfBirthDay.required");
    assertMessages(errors, "Enter a complete date of birth");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_monthNull() {
    var isValid = invokeValidateDate("21", null, "2020", errors);

    assertCodes(errors, "dateOfBirthMonth.required");
    assertMessages(errors, "Enter a complete date of birth");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_yearNull() {
    var isValid = invokeValidateDate("21", "12", null, errors);

    assertCodes(errors, "dateOfBirthYear.required");
    assertMessages(errors, "Enter a complete date of birth");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_dayAndMonthNull() {
    var isValid = invokeValidateDate(null, null, "2020", errors);

    assertCodes(errors, "dateOfBirthDay.required", "dateOfBirthMonth.required");
    assertMessages(errors, "Enter a complete date of birth", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_monthAndYearNull() {
    var isValid = invokeValidateDate("21", null, null, errors);

    assertCodes(errors, "dateOfBirthMonth.required", "dateOfBirthYear.required");
    assertMessages(errors, "Enter a complete date of birth", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_dayAndYearNull() {
    var isValid = invokeValidateDate(null, "12", null, errors);

    assertCodes(errors, "dateOfBirthDay.required", "dateOfBirthYear.required");
    assertMessages(errors, "Enter a complete date of birth", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_dayInvalid() {
    var isValid = invokeValidateDate("foo", "12", "2020", errors);

    assertCodes(errors, "dateOfBirthDay.invalid");
    assertMessages(errors, "Date of birth must be a real date");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_monthInvalid() {
    var isValid = invokeValidateDate("21", "foo", "2020", errors);

    assertCodes(errors, "dateOfBirthMonth.invalid");
    assertMessages(errors, "Date of birth must be a real date");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_yearInvalid() {
    var isValid = invokeValidateDate("21", "12", "foo", errors);

    assertCodes(errors, "dateOfBirthYear.invalid");
    assertMessages(errors, "Date of birth must be a real date");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_dayAndMonthInvalid() {
    var isValid = invokeValidateDate("2.3", "a1", "2020", errors);

    assertCodes(errors, "dateOfBirthDay.invalid", "dateOfBirthMonth.invalid");
    assertMessages(errors, "Date of birth must be a real date", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_monthAndYearInvalid() {
    var isValid = invokeValidateDate("21", "-4", "abc", errors);

    assertCodes(errors, "dateOfBirthMonth.invalid", "dateOfBirthYear.invalid");
    assertMessages(errors, "Date of birth must be a real date", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_dayAndYearInvalid() {
    var isValid = invokeValidateDate("99", "12", "<>?/!-=", errors);

    assertCodes(errors, "dateOfBirthDay.invalid", "dateOfBirthYear.invalid");
    assertMessages(errors, "Date of birth must be a real date", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDate_dateInvalid() {
    var isValid = invokeValidateDate("29", "02", "2021", errors);

    assertCodes(errors, "dateOfBirthDay.invalid", "dateOfBirthMonth.invalid", "dateOfBirthYear.invalid");
    assertMessages(errors, "Date of birth must be a real date", "", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDateIsTodayOrInTheFuture_today() {
    var now = LocalDate.now();
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsTodayOrInTheFuture(day, month, year, errors);

    assertThat(errors.getAllErrors()).isEmpty();
    assertThat(isValid).isTrue();
  }

  @Test
  void validateDateIsTodayOrInTheFuture_future() {
    var now = LocalDate.now().plusDays(1);
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsTodayOrInTheFuture(day, month, year, errors);

    assertThat(errors.getAllErrors()).isEmpty();
    assertThat(isValid).isTrue();
  }

  @Test
  void validateDateIsTodayOrInTheFuture_past() {
    var now = LocalDate.now().minusDays(1);
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsTodayOrInTheFuture(day, month, year, errors);

    assertCodes(errors, "dateOfBirthDay.notAfterTargetDate", "dateOfBirthMonth.notAfterTargetDate", "dateOfBirthYear.notAfterTargetDate");
    assertMessages(errors, "Date of birth must be today or in the future", "", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDateIsTodayOrInTheFuture_invalid() {
    var isValid = invokeValidateDateIsTodayOrInTheFuture("99", "foo", "2020", errors);

    assertCodes(errors, "dateOfBirthDay.invalid", "dateOfBirthMonth.invalid");
    assertMessages(errors, "Date of birth must be a real date", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDateIsInTheFuture_future() {
    var now = LocalDate.now().plusDays(1);
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsInTheFuture(day, month, year, errors);

    assertThat(errors.getAllErrors()).isEmpty();
    assertThat(isValid).isTrue();
  }

  @Test
  void validateDateIsInTheFuture_today() {
    var now = LocalDate.now();
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsInTheFuture(day, month, year, errors);

    assertCodes(errors, "dateOfBirthDay.notAfterTargetDate", "dateOfBirthMonth.notAfterTargetDate", "dateOfBirthYear.notAfterTargetDate");
    assertMessages(errors, "Date of birth must be in the future", "", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDateIsInTheFuture_past() {
    var now = LocalDate.now().minusDays(1);
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsInTheFuture(day, month, year, errors);

    assertCodes(errors, "dateOfBirthDay.notAfterTargetDate", "dateOfBirthMonth.notAfterTargetDate", "dateOfBirthYear.notAfterTargetDate");
    assertMessages(errors, "Date of birth must be in the future", "", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDateIsInTheFuture_invalid() {
    var isValid = invokeValidateDateIsInTheFuture("99", "foo", "2020", errors);

    assertCodes(errors, "dateOfBirthDay.invalid", "dateOfBirthMonth.invalid");
    assertMessages(errors, "Date of birth must be a real date", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDateIsTodayOrInThePast_today() {
    var now = LocalDate.now();
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsTodayOrInThePast(day, month, year, errors);

    assertThat(errors.getAllErrors()).isEmpty();
    assertThat(isValid).isTrue();
  }

  @Test
  void validateDateIsTodayOrInThePast_past() {
    var now = LocalDate.now().minusDays(1);
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsTodayOrInThePast(day, month, year, errors);

    assertThat(errors.getAllErrors()).isEmpty();
    assertThat(isValid).isTrue();
  }

  @Test
  void validateDateIsTodayOrInThePast_future() {
    var now = LocalDate.now().plusDays(1);
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsTodayOrInThePast(day, month, year, errors);

    assertCodes(errors, "dateOfBirthDay.notBeforeTargetDate", "dateOfBirthMonth.notBeforeTargetDate", "dateOfBirthYear.notBeforeTargetDate");
    assertMessages(errors, "Date of birth must be today or in the past", "", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDateIsTodayOrInThePast_invalid() {
    var isValid = invokeValidateDateIsTodayOrInThePast("99", "foo", "2020", errors);

    assertCodes(errors, "dateOfBirthDay.invalid", "dateOfBirthMonth.invalid");
    assertMessages(errors, "Date of birth must be a real date", "");
    assertThat(isValid).isFalse();
  }


  @Test
  void validateDateIsInThePast_past() {
    var now = LocalDate.now().minusDays(1);
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsInThePast(day, month, year, errors);

    assertThat(errors.getAllErrors()).isEmpty();
    assertThat(isValid).isTrue();
  }

  @Test
  void validateDateIsInThePast_today() {
    var now = LocalDate.now();
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsInThePast(day, month, year, errors);

    assertCodes(errors, "dateOfBirthDay.notBeforeTargetDate", "dateOfBirthMonth.notBeforeTargetDate", "dateOfBirthYear.notBeforeTargetDate");
    assertMessages(errors, "Date of birth must be in the past", "", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDateIsInThePast_future() {
    var now = LocalDate.now().plusDays(1);
    var day = String.valueOf(now.getDayOfMonth());
    var month = String.valueOf(now.getMonthValue());
    var year = String.valueOf(now.getYear());

    var isValid = invokeValidateDateIsInThePast(day, month, year, errors);

    assertCodes(errors, "dateOfBirthDay.notBeforeTargetDate", "dateOfBirthMonth.notBeforeTargetDate", "dateOfBirthYear.notBeforeTargetDate");
    assertMessages(errors, "Date of birth must be in the past", "", "");
    assertThat(isValid).isFalse();
  }

  @Test
  void validateDateIsInThePast_invalid() {
    var isValid = invokeValidateDateIsInThePast("99", "foo", "2020", errors);

    assertCodes(errors, "dateOfBirthDay.invalid", "dateOfBirthMonth.invalid");
    assertMessages(errors, "Date of birth must be a real date", "");
    assertThat(isValid).isFalse();
  }


  private void assertCodes(Errors errors, String... codes) {
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactly(codes);
  }

  private void assertMessages(Errors errors, String... message) {
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getDefaultMessage)
        .containsExactly(message);
  }

  private boolean invokeValidateDate(String day, String month, String year, Errors errors) {
    return DateValidationUtil.validateDate(
        "dateOfBirth",
        "date of birth",
        day,
        month,
        year,
        errors
    );
  }

  private boolean invokeValidateDateIsTodayOrInTheFuture(String day, String month, String year, Errors errors) {
    return DateValidationUtil.validateDateIsTodayOrInTheFuture(
        "dateOfBirth",
        "date of birth",
        day,
        month,
        year,
        errors
    );
  }

  private boolean invokeValidateDateIsInTheFuture(String day, String month, String year, Errors errors) {
    return DateValidationUtil.validateDateIsInTheFuture(
        "dateOfBirth",
        "date of birth",
        day,
        month,
        year,
        errors
    );
  }

  private boolean invokeValidateDateIsTodayOrInThePast(String day, String month, String year, Errors errors) {
    return DateValidationUtil.validateDateIsTodayOrInThePast(
        "dateOfBirth",
        "date of birth",
        day,
        month,
        year,
        errors
    );
  }

  private boolean invokeValidateDateIsInThePast(String day, String month, String year, Errors errors) {
    return DateValidationUtil.validateDateIsInThePast(
        "dateOfBirth",
        "date of birth",
        day,
        month,
        year,
        errors
    );
  }

  private static class DateValidationTestForm {

    private Integer dateOfBirthDay;
    private Integer dateOfBirthMonth;
    private Integer dateOfBirthYear;

    DateValidationTestForm(Integer dateOfBirthDay, Integer dateOfBirthMonth, Integer dateOfBirthYear) {
      this.dateOfBirthDay = dateOfBirthDay;
      this.dateOfBirthMonth = dateOfBirthMonth;
      this.dateOfBirthYear = dateOfBirthYear;
    }

    public Integer getDateOfBirthDay() {
      return dateOfBirthDay;
    }

    public void setDateOfBirthDay(Integer dateOfBirthDay) {
      this.dateOfBirthDay = dateOfBirthDay;
    }

    public Integer getDateOfBirthMonth() {
      return dateOfBirthMonth;
    }

    public void setDateOfBirthMonth(Integer dateOfBirthMonth) {
      this.dateOfBirthMonth = dateOfBirthMonth;
    }

    public Integer getDateOfBirthYear() {
      return dateOfBirthYear;
    }

    public void setDateOfBirthYear(Integer dateOfBirthYear) {
      this.dateOfBirthYear = dateOfBirthYear;
    }
  }
}