package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class ConfirmNominationAppointmentValidatorTest {

  private static final String VALID_EXTENSION = "valid-extension";
  private static final FileUploadProperties FILE_UPLOAD_PROPERTIES = FileUploadPropertiesTestUtil.builder()
      .withDefaultPermittedFileExtensions(Set.of("default-extension", VALID_EXTENSION))
      .build();
  private static final Instant INSTANT_NOW = Instant.now();
  private static final LocalDate LOCAL_DATE_NOW = LocalDate.ofInstant(INSTANT_NOW, ZoneId.systemDefault());
  private static final Clock CLOCK = Clock.fixed(INSTANT_NOW, ZoneId.systemDefault());

  @Mock
  private CaseEventQueryService caseEventQueryService;

  private ConfirmNominationAppointmentValidator confirmNominationAppointmentValidator;

  @BeforeEach
  void setUp() {
    confirmNominationAppointmentValidator = new ConfirmNominationAppointmentValidator(
        CLOCK,
        caseEventQueryService,
        FILE_UPLOAD_PROPERTIES
    );
  }

  @Test
  void validate_emptyForm() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(LOCAL_DATE_NOW));

    var form = new ConfirmNominationAppointmentForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .contains(
            entry("appointmentDate.dayInput.inputValue",
                Set.of("Enter a complete Appointment date")),
            entry("appointmentDate.monthInput.inputValue", Set.of("")),
            entry("appointmentDate.yearInput.inputValue", Set.of(""))
        );
  }

  @Test
  void validate_validForm() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(LOCAL_DATE_NOW));

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().setDate(LOCAL_DATE_NOW);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenFormIsFullyPopulated_andDocumentHasValidExtension_thenVerifyErrors() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var form = new ConfirmNominationAppointmentForm();
    form.getComments().setInputValue("Subject");
    form.getAppointmentDate().setDate(LOCAL_DATE_NOW);

    var decisionDate = LOCAL_DATE_NOW.minusDays(2);
    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(decisionDate));

    var uploadedFile = UploadedFileTestUtil.newBuilder()
        .withName("document.%s".formatted(VALID_EXTENSION))
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());
    uploadedFileForm.setFileDescription(uploadedFile.getDescription());

    form.getFiles().add(uploadedFileForm);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);

    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenDecisionDateAndAppointmentDateAreTheSame_thenNoError() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(LOCAL_DATE_NOW));

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().setDate(LOCAL_DATE_NOW);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .doesNotContainKeys(
            "appointmentDate.dayInput.inputValue",
            "appointmentDate.monthInput.inputValue",
            "appointmentDate.yearInput.inputValue"
        );
  }

  @Test
  void validate_whenAppointmentDateInFuture_thenVerifyError() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(LOCAL_DATE_NOW));

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().setDate(LOCAL_DATE_NOW.plusDays(1));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var formattedDate = DateUtil.formatShortDate(LOCAL_DATE_NOW);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .contains(
            entry("appointmentDate.dayInput.inputValue",
                Set.of("Appointment date must be the same as or before %s".formatted(formattedDate))),
            entry("appointmentDate.monthInput.inputValue", Set.of("")),
            entry("appointmentDate.yearInput.inputValue", Set.of(""))
        );
  }

  @Test
  void validate_whenAppointmentDateIsBeforeDecisionDate_thenVerifyError() {
    var decisionDate = LOCAL_DATE_NOW.minusDays(2);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(decisionDate));

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().setDate(decisionDate.minusDays(1));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var formattedDate = DateUtil.formatShortDate(decisionDate);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .contains(
            entry("appointmentDate.dayInput.inputValue",
                Set.of("Appointment date must be the same as or after %s".formatted(formattedDate))),
            entry("appointmentDate.monthInput.inputValue", Set.of("")),
            entry("appointmentDate.yearInput.inputValue", Set.of(""))
        );
  }

  @Test
  void validate_whenAppointmentDateHasInvalidCharacters_thenVerifyError() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(LOCAL_DATE_NOW));

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().getDayInput().setInputValue("a");
    form.getAppointmentDate().getMonthInput().setInputValue("b");
    form.getAppointmentDate().getYearInput().setInputValue("c");

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .contains(
            entry("appointmentDate.dayInput.inputValue",
                Set.of("Appointment date must be a real date")),
            entry("appointmentDate.monthInput.inputValue", Set.of("")),
            entry("appointmentDate.yearInput.inputValue", Set.of(""))
        );
  }

  @Test
  void validate_whenFileExtensionIsUnsupported_thenVerifyErrors() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var form = new ConfirmNominationAppointmentForm();
    form.getComments().setInputValue("Subject");
    form.getAppointmentDate().setDate(LOCAL_DATE_NOW);

    var decisionDate = LOCAL_DATE_NOW.minusDays(2);
    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(decisionDate));

    var uploadedFile = UploadedFileTestUtil.newBuilder()
        .withName("document.invalid-extension")
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());
    uploadedFileForm.setFileDescription(uploadedFile.getDescription());

    form.getFiles().add(uploadedFileForm);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);

    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    var allowedExtensions = String.join(", ", FILE_UPLOAD_PROPERTIES.defaultPermittedFileExtensions());

    assertThat(errors)
        .containsExactly(
            entry("files", Set.of(
                "The selected files must be a %s".formatted(allowedExtensions)
            ))
        );
  }

  @Test
  void validate_onTwoArgsConstructorCall_verifyError() {
    var form = new ConfirmNominationAppointmentForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    assertThatThrownBy(() -> confirmNominationAppointmentValidator.validate(form, bindingResult))
        .hasMessage("Expected a %s but none was provided".formatted(
            ConfirmNominationAppointmentValidatorHint.class));
  }

  @Test
  void supports_verifyUnsupported() {
    var result = confirmNominationAppointmentValidator.supports(UnsupportedClass.class);
    assertFalse(result);
  }

  @Test
  void supports_verifySupported() {
    var result = confirmNominationAppointmentValidator.supports(ConfirmNominationAppointmentForm.class);
    assertTrue(result);
  }

  public static class UnsupportedClass {
  }
}