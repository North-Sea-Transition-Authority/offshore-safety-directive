package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
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
import org.springframework.validation.FieldError;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileFormTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class AppointmentTerminationValidatorTest {

  private static final String VALID_EXTENSION = UploadedFileFormTestUtil.VALID_FILE_EXTENSION;
  private static final FileUploadProperties FILE_UPLOAD_PROPERTIES = FileUploadPropertiesTestUtil.builder()
      .withDefaultPermittedFileExtensions(Set.of(VALID_EXTENSION))
      .build();

  private AppointmentTerminationValidator appointmentTerminationValidator;
  private AppointmentTerminationValidatorHint validatorHint;

  private static final String TERMINATION_DOCUMENT_ERROR_MESSAGE = "You must upload a supporting document";

  @BeforeEach
  void setUp() {
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    validatorHint = new AppointmentTerminationValidatorHint(appointmentDto);
    appointmentTerminationValidator = new AppointmentTerminationValidator(FILE_UPLOAD_PROPERTIES);
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

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("reason.inputValue", "reason.required", "Enter a reason for the termination"),
            tuple(
                "terminationDate.dayInput.inputValue",
                "terminationDate.dayInput.required",
                "Enter a complete termination date"
            ),
            tuple("terminationDate.monthInput.inputValue", "terminationDate.monthInput.required", ""),
            tuple("terminationDate.yearInput.inputValue", "terminationDate.yearInput.required", ""),
            tuple("terminationDocuments", "terminationDocuments.belowThreshold", TERMINATION_DOCUMENT_ERROR_MESSAGE)
          );
  }

  @Test
  void validate_whenFullyPopulated_thenNoErrors() {
    var form = new AppointmentTerminationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var uploadedFileForm = UploadedFileFormTestUtil.builder()
        .withFileDescription("test")
        .build();

    form.getTerminationDate().setDate(LocalDate.now());
    form.getReason().setInputValue("reason");
    form.setTerminationDocuments(List.of(uploadedFileForm));

    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenTerminationDatePartiallyEntered_thenVerifyError() {
    var form = AppointmentTerminationFormTestUtil.builder().build();

    form.getTerminationDate().setDay(10);
    form.getTerminationDate().getMonthInput().setInputValue(null);
    form.getTerminationDate().setYear(LocalDate.now().getYear());

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "terminationDate.monthInput.inputValue",
                "terminationDate.monthInput.required",
                "Enter a complete termination date"
            )
        );
  }

  @Test
  void validate_whenTerminationDateInvalidStringEntered_thenVerifyError() {
    var form = AppointmentTerminationFormTestUtil.builder().build();

    form.getTerminationDate().getDayInput().setInputValue("non valid day");
    form.getTerminationDate().getMonthInput().setInputValue("non valid month");
    form.getTerminationDate().getYearInput().setInputValue("non valid year");

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "terminationDate.dayInput.inputValue",
                "terminationDate.dayInput.invalid",
                "Termination date must be a real date"
            ),
            tuple(
                "terminationDate.monthInput.inputValue",
                "terminationDate.monthInput.invalid",
                ""
            ),
            tuple(
                "terminationDate.yearInput.inputValue",
                "terminationDate.yearInput.invalid",
                ""
            )
        );
  }

  @Test
  void validate_whenTerminationDateBeforeFromDate_thenVerifyError() {
    var form = AppointmentTerminationFormTestUtil.builder().build();
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

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "terminationDate.dayInput.inputValue",
                "terminationDate.dayInput.minDateNotMet",
                "Termination date must be the same as or after %s".formatted(DateUtil.formatShortDate(appointmentFromDate))
            ),
            tuple(
                "terminationDate.monthInput.inputValue",
                "terminationDate.monthInput.minDateNotMet",
                ""
            ),
            tuple(
                "terminationDate.yearInput.inputValue",
                "terminationDate.yearInput.minDateNotMet",
                ""
            )
        );
  }

  @Test
  void validate_whenTerminationDateAfterCurrentDate_thenVerifyError() {
    var form = AppointmentTerminationFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var currentDate = LocalDate.now();
    var dateAfterAppointmentDate = currentDate.plusDays(1);

    form.getTerminationDate().setDate(dateAfterAppointmentDate);

    var appointment = AppointmentDtoTestUtil.builder()
        .withAppointmentToDate(dateAfterAppointmentDate)
        .build();
    var validatorHintWithToDate = new AppointmentTerminationValidatorHint(appointment);

    appointmentTerminationValidator.validate(form, bindingResult, validatorHintWithToDate);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "terminationDate.dayInput.inputValue",
                "terminationDate.dayInput.maxDateExceeded",
                "Termination date must be the same as or before %s".formatted(DateUtil.formatShortDate(currentDate))
            ),
            tuple(
                "terminationDate.monthInput.inputValue",
                "terminationDate.monthInput.maxDateExceeded",
                ""
            ),
            tuple(
                "terminationDate.yearInput.inputValue",
                "terminationDate.yearInput.maxDateExceeded",
                ""
            )
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

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "reason.inputValue",
                "reason.required",
                "Enter a reason for the termination"
            )
        );
  }

  @Test
  void validate_whenNoFileDescription_thenValidationErrors() {
    var uploadedFileForm = UploadedFileFormTestUtil.builder()
        .withFileDescription(null)
        .build();
    var form = AppointmentTerminationFormTestUtil.builder()
        .withTerminationDocuments(List.of(uploadedFileForm))
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentTerminationValidator.validate(form, bindingResult, validatorHint);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "terminationDocuments[0].uploadedFileDescription",
                "terminationDocuments[0].uploadedFileDescription.required",
                "Enter a description of this file"
            )
        );
  }

  private static class UnsupportedClass {
  }
}