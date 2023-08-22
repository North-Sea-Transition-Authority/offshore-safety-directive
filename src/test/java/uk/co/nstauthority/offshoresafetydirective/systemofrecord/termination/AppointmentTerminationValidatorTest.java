package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadFormTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class AppointmentTerminationValidatorTest {

  private AppointmentTerminationValidator appointmentTerminationValidator;
  private AppointmentTerminationValidatorHint validatorHint;

  @BeforeEach
  void setUp() {
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    validatorHint = new AppointmentTerminationValidatorHint(appointmentDto);
    appointmentTerminationValidator = new AppointmentTerminationValidator();
  }

  @Test
  void supports_doesSupport() {
    assertTrue(appointmentTerminationValidator.supports(AppointmentTerminationForm.class));
  }

  @Test
  void supports_doesNotSupport() {
    assertFalse(appointmentTerminationValidator.supports(UnsupportedClass.class));
  }

  @Test
  void validate_whenNoValidationHints_thenThrowException() {
    var form = new AppointmentTerminationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    assertThatThrownBy(() -> appointmentTerminationValidator.validate(form, bindingResult))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Expected validator hint to be used");
  }

  @Test
  void validate_whenNoFieldsPopulated_thenVerifyHasErrors() {
    var form = new AppointmentTerminationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors).containsExactly(
        entry("terminationDate.dayInput.inputValue", Set.of("Enter a complete Termination date")),
        entry("terminationDate.monthInput.inputValue", Set.of("")),
        entry("terminationDate.yearInput.inputValue", Set.of("")),
        entry("reason.inputValue", Set.of("Enter a reason for the termination")),
        entry("terminationDocuments", Set.of("Upload a document"))
    );
  }

  @Test
  void validate_whenFullyPopulated_thenNoErrors() {
    var form = new AppointmentTerminationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var fileUploadForm = FileUploadFormTestUtil.builder()
        .withUploadedFileDescription("test")
        .build();

    form.getTerminationDate().setDate(LocalDate.now());
    form.getReason().setInputValue("reason");
    form.setTerminationDocuments(List.of(fileUploadForm));

    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenTerminationDatePartiallyEntered_thenVerifyError() {
    var form = new AppointmentTerminationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    form.getTerminationDate().setDay(10);
    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);
    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors).extractingByKeys(
        "terminationDate.dayInput.inputValue",
        "terminationDate.monthInput.inputValue",
        "terminationDate.yearInput.inputValue"
        ).containsExactly(
        null,
        Set.of("Enter a complete Termination date"),
        Set.of("")
    );
  }

  @Test
  void validate_whenTerminationDateInvalidStringEntered_thenVerifyError() {
    var form = new AppointmentTerminationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    form.getTerminationDate().getDayInput().setInputValue("non valid day");
    form.getTerminationDate().getMonthInput().setInputValue("non valid month");
    form.getTerminationDate().getYearInput().setInputValue("non valid year");

    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);
    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors).extractingByKeys(
        "terminationDate.dayInput.inputValue",
        "terminationDate.monthInput.inputValue",
        "terminationDate.yearInput.inputValue"
    ).containsExactly(
        Set.of("Termination date must be a real date"),
        Set.of(""),
        Set.of("")
    );
  }

  @Test
  void validate_whenTerminationDateBeforeFromDate_thenVerifyError() {
    var form = new AppointmentTerminationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var appointmentFromDate = LocalDate.of(2023, 8, 8);
    var dateBeforeAppointmentFromDate = appointmentFromDate.minusDays(1);

    form.getTerminationDate().setDate(dateBeforeAppointmentFromDate);

    var appointment = AppointmentDtoTestUtil.builder()
        .withAppointmentToDate(dateBeforeAppointmentFromDate)
        .withAppointmentFromDate(appointmentFromDate)
        .build();
    var validatorHintWithToDate = new AppointmentTerminationValidatorHint(appointment);

    appointmentTerminationValidator.validate(form, bindingResult, validatorHintWithToDate);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors).extractingByKeys(
        "terminationDate.dayInput.inputValue",
        "terminationDate.monthInput.inputValue",
        "terminationDate.yearInput.inputValue"
    ).containsExactly(
        Set.of("Termination date must be the same as or after %s".formatted(DateUtil.formatShortDate(appointmentFromDate))),
        Set.of(""),
        Set.of("")
    );
  }

  @Test
  void validate_whenTerminationDateAfterCurrentDate_thenVerifyError() {
    var form = new AppointmentTerminationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var currentDate = LocalDate.now();
    var dateAfterAppointmentDate = currentDate.plusDays(1);

    form.getTerminationDate().setDate(dateAfterAppointmentDate);

    var appointment = AppointmentDtoTestUtil.builder()
        .withAppointmentToDate(dateAfterAppointmentDate)
        .build();
    var validatorHintWithToDate = new AppointmentTerminationValidatorHint(appointment);

    appointmentTerminationValidator.validate(form, bindingResult, validatorHintWithToDate);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors).extractingByKeys(
        "terminationDate.dayInput.inputValue",
        "terminationDate.monthInput.inputValue",
        "terminationDate.yearInput.inputValue"
    ).containsExactly(
        Set.of("Termination date must be the same as or before %s".formatted(DateUtil.formatShortDate(currentDate))),
        Set.of(""),
        Set.of("")
    );
  }

  @Test
  void validate_whenTerminationDateIsToday_thenVerifyNoError() {
    var form = AppointmentTerminationFormTestUtil.builder()
        .withTerminationDate(LocalDate.now())
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var dateBeforeAppointmentFromDate = LocalDate.now().minusDays(1);

    var appointment = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(dateBeforeAppointmentFromDate)
        .build();
    var validatorHintWithToDate = new AppointmentTerminationValidatorHint(appointment);

    appointmentTerminationValidator.validate(form, bindingResult, validatorHintWithToDate);
    assertFalse(form.getTerminationDate().fieldHasErrors(bindingResult));
  }

  @Test
  void validate_whenTerminationDateIsEqualToFromDate_thenVerifyNoError() {
    var dateBeforeAppointmentFromDate = LocalDate.now().minusDays(1);
    var form = AppointmentTerminationFormTestUtil.builder()
        .withTerminationDate(dateBeforeAppointmentFromDate)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var appointment = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(dateBeforeAppointmentFromDate)
        .build();
    var validatorHintWithToDate = new AppointmentTerminationValidatorHint(appointment);

    appointmentTerminationValidator.validate(form, bindingResult, validatorHintWithToDate);
    assertFalse(form.getTerminationDate().fieldHasErrors(bindingResult));
  }

  @Test
  void validate_whenReasonIsNotNull_thenVerifyNoError() {
    var form = AppointmentTerminationFormTestUtil.builder()
        .withReason("valid reason")
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);
    assertFalse(form.getReason().fieldHasErrors(bindingResult));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenReasonIsNull_thenVerifyError(String reason) {
    var form = AppointmentTerminationFormTestUtil.builder()
        .withReason(reason)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors).extractingByKeys(
        "reason.inputValue"
    ).containsExactly(
        Set.of("Enter a reason for the termination")
    );
  }

  @Test
  void validate_whenNoFileDescription_thenValidationErrors() {
    var fileUploadForm = FileUploadFormTestUtil.builder()
        .withUploadedFileDescription(null)
        .build();
    var form = AppointmentTerminationFormTestUtil.builder()
        .withTerminationDocuments(List.of(fileUploadForm))
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("terminationDocuments[0].uploadedFileDescription", Set.of("terminationDocuments[0].uploadedFileDescription.required"))
    );
  }

  private static class UnsupportedClass {
  }
}