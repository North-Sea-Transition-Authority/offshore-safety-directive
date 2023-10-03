package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.test.util.ReflectionTestUtils;

class SystemOfRecordSearchFormTest {

  @Test
  void isEmpty_whenEmpty_thenTrue() {
    var form = new SystemOfRecordSearchForm();
    assertTrue(form.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("getSystemOfRecordSearchFormFields")
  void isEmpty_whenNotEmpty_thenFalse(Field field) {

    var form = new SystemOfRecordSearchForm();

    Object fieldValue;

    if (field.getType().equals(Integer.class)) {
      fieldValue = 1;
    } else if (field.getType().equals(String.class)) {
      fieldValue = "NON NULL VALUE";
    } else {
      throw new IllegalStateException("Unsupported field type %s".formatted(field.getType().getSimpleName()));
    }

    ReflectionTestUtils.setField(form, field.getName(), fieldValue);

    assertFalse(form.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("getSystemOfRecordSearchFormFields")
  void isEmptyExcept_whenOnlyExcludedFieldsNotNull_thenTrue(Field field) {

    var form = new SystemOfRecordSearchForm();

    ReflectionTestUtils.setField(form, field.getName(), getNonNullValueForFieldType(field));

    assertTrue(form.isEmptyExcept(field.getName()));
  }

  @Test
  void isEmptyExcept_whenEmptyFormAndNoExceptions_thenTrue() {
    var form = new SystemOfRecordSearchForm();
    assertTrue(form.isEmptyExcept());
  }

  @Test
  void isEmptyExcept_whenPopulatedFormNoExceptions_thenFalse() {
    var form = new SystemOfRecordSearchForm();
    form.setAppointedOperatorId(100);
    assertFalse(form.isEmptyExcept());
  }

  @Test
  void isEmptyExcept_whenExcludingNonNullFields_thenTrue() {
    var form = new SystemOfRecordSearchForm();
    form.setAppointedOperatorId(100);
    assertTrue(form.isEmptyExcept("appointedOperatorId"));
  }

  @Test
  void isEmptyExcept_whenNotValidFieldNameAndAllFieldValuesNull_thenTrue() {
    var form = new SystemOfRecordSearchForm();
    assertTrue(form.isEmptyExcept("NOT_A_FIELD_NAME"));
  }

  @Test
  void build_whenAppointedOperatorIdIsOfWrongType_thenNull() {
    var form = SystemOfRecordSearchForm.builder()
        .withAppointedOperatorId("fish")
        .build();

    assertThat(form.getAppointedOperatorId()).isNull();
  }

  @Test
  void build_whenWellboreIdIsOfWrongType_thenNull() {
    var form = SystemOfRecordSearchForm.builder()
        .withWellbore("fish")
        .build();

    assertThat(form.getWellboreId()).isNull();
  }

  @Test
  void build_whenInstallationIdIsOfWrongType_thenNull() {
    var form = SystemOfRecordSearchForm.builder()
        .withInstallation("fish")
        .build();

    assertThat(form.getInstallationId()).isNull();
  }

  @Test
  void build_whenLicenceIdIsOfWrongType_thenNull() {
    var form = SystemOfRecordSearchForm.builder()
        .withLicence("fish")
        .build();

    assertThat(form.getLicenceId()).isNull();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void build_whenNullOrEmpty_thenNullReturned(String inputToTest) {

    var searchForm = SystemOfRecordSearchForm.builder()
        .withAppointedOperatorId(inputToTest)
        .withWellbore(inputToTest)
        .withInstallation(inputToTest)
        .withLicence(inputToTest)
        .withSubarea(inputToTest)
        .build();

    assertThat(searchForm).hasAllNullFieldsOrProperties();
  }

  @Test
  void build_whenInputNotBlank_thenValueReturned() {

    var searchForm = SystemOfRecordSearchForm.builder()
        .withAppointedOperatorId("100")
        .withWellbore("200")
        .withInstallation("300")
        .withLicence("400")
        .withSubarea("500")
        .build();

    assertThat(searchForm)
        .extracting(
            SystemOfRecordSearchForm::getAppointedOperatorId,
            SystemOfRecordSearchForm::getWellboreId,
            SystemOfRecordSearchForm::getInstallationId,
            SystemOfRecordSearchForm::getLicenceId,
            SystemOfRecordSearchForm::getSubareaId
        )
        .containsExactly(
            100,
            200,
            300,
            400,
            "500"
        );
  }

  private static Stream<Arguments> getSystemOfRecordSearchFormFields() {
    return Arrays.stream(SystemOfRecordSearchForm.class.getDeclaredFields())
        .map(field -> Arguments.of(Named.of(field.getName(), field)));
  }

  private Object getNonNullValueForFieldType(Field field) {
    if (field.getType().equals(Integer.class)) {
      return  1;
    } else if (field.getType().equals(String.class)) {
      return "NON NULL VALUE";
    } else {
      throw new IllegalStateException("Unsupported field type %s".formatted(field.getType().getSimpleName()));
    }
  }

}