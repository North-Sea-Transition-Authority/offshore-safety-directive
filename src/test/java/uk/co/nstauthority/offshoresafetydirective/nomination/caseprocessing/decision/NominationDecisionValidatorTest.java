package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class NominationDecisionValidatorTest {

  private static final String VALID_EXTENSION = "pdf";
  private static final FileUploadProperties FILE_UPLOAD_PROPERTIES = FileUploadPropertiesTestUtil.builder()
      .withDefaultPermittedFileExtensions(Set.of("default-extension", VALID_EXTENSION))
      .build();

  private static final int SUBMITTED_DAYS_PRIOR = 5;

  private Clock clockNow;
  private NominationDecisionValidator nominationDecisionValidator;
  private NominationDecisionValidatorHint validatorHint;
  private NominationDetail nominationDetail;

  @BeforeEach
  void setUp() {
    clockNow = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    nominationDecisionValidator = new NominationDecisionValidator(FILE_UPLOAD_PROPERTIES, clockNow);
    nominationDetail = NominationDetailTestUtil.builder()
        .withSubmittedInstant(clockNow.instant().minus(Period.ofDays(SUBMITTED_DAYS_PRIOR)))
        .build();
    validatorHint = new NominationDecisionValidatorHint(nominationDetail);
  }

  @Test
  void validate_whenNoFieldsPopulated_thenVerifyHasErrors() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors).containsExactly(
        entry(
            NominationDecisionValidator.NOMINATION_DECISION_FIELD_NAME,
            Set.of(NominationDecisionValidator.NOMINATION_DECISION_BLANK_ERROR_MESSAGE)
        ),
        entry("decisionDate.dayInput.inputValue", Set.of("Enter a complete Decision date")),
        entry("decisionDate.monthInput.inputValue", Set.of("")),
        entry("decisionDate.yearInput.inputValue", Set.of("")),
        entry("comments.inputValue", Set.of("Enter Decision comments")),
        entry("decisionFiles", Set.of("Upload a decision document"))
    );

  }

  @Test
  void validate_whenFullyPopulated_thenNoErrors() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    nominationDecisionForm.setNominationDecision(NominationDecision.NO_OBJECTION);
    nominationDecisionForm.getDecisionDate().setDate(LocalDate.ofInstant(clockNow.instant(), ZoneId.systemDefault()));
    nominationDecisionForm.getComments().setInputValue("comment text");

    var uploadedFile = UploadedFileTestUtil.builder()
        .withName("document.%s".formatted(VALID_EXTENSION))
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());
    uploadedFileForm.setFileDescription(uploadedFile.getDescription());

    nominationDecisionForm.getDecisionFiles().add(uploadedFileForm);

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenNominationDecisionNotNullButInvalidString_thenVerifyError() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    nominationDecisionForm.setNominationDecision("TEST_VALUE");

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);

    assertThat(bindingResult.getFieldError("nominationDecision"))
        .extracting(
            FieldError::getField,
            DefaultMessageSourceResolvable::getCode,
            DefaultMessageSourceResolvable::getDefaultMessage
        ).containsExactly(
            NominationDecisionValidator.NOMINATION_DECISION_FIELD_NAME,
            NominationDecisionValidator.NOMINATION_DECISION_BLANK_ERROR_CODE,
            NominationDecisionValidator.NOMINATION_DECISION_BLANK_ERROR_MESSAGE
        );
  }

  @Test
  void validate_whenDecisionDateIsToday_thenVerifyNoError() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    var validDecisionDate = LocalDate.ofInstant(clockNow.instant(), ZoneId.systemDefault());
    nominationDecisionForm.getDecisionDate().setDate(validDecisionDate);

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);
    assertFalse(nominationDecisionForm.getDecisionDate().fieldHasErrors(bindingResult));
  }

  @Test
  void validate_whenDecisionDateIsSubmissionDate_thenVerifyNoError() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    var submittedDate = LocalDate.ofInstant(nominationDetail.getSubmittedInstant(), ZoneId.systemDefault());

    nominationDecisionForm.getDecisionDate().setDate(submittedDate);

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);
    assertFalse(nominationDecisionForm.getDecisionDate().fieldHasErrors(bindingResult));
  }

  @Test
  void validate_whenDecisionDatePartiallyEntered_thenVerifyError() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    var validDecisionDate = LocalDate.ofInstant(clockNow.instant(), ZoneId.systemDefault());
    nominationDecisionForm.getDecisionDate().getDayInput().setInteger(validDecisionDate.getDayOfMonth());

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors).extractingByKeys(
        "decisionDate.dayInput.inputValue",
        "decisionDate.monthInput.inputValue",
        "decisionDate.yearInput.inputValue"
    ).containsExactly(
        null,
        Set.of("Enter a complete Decision date"),
        Set.of("")
    );

  }

  @Test
  void validate_whenDecisionDateInvalidStringEntered_thenVerifyError() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    nominationDecisionForm.getDecisionDate().getDayInput().setInputValue("non valid day");
    nominationDecisionForm.getDecisionDate().getMonthInput().setInputValue("non valid month");
    nominationDecisionForm.getDecisionDate().getYearInput().setInputValue("non valid year");

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors).extractingByKeys(
        "decisionDate.dayInput.inputValue",
        "decisionDate.monthInput.inputValue",
        "decisionDate.yearInput.inputValue"
    ).containsExactly(
        Set.of("Decision date must be a real date"),
        Set.of(""),
        Set.of("")
    );

  }

  @Test
  void validate_whenDecisionDateBeforeSubmittedDate_thenVerifyError() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    var submittedDate = LocalDate.ofInstant(clockNow.instant(), ZoneId.systemDefault())
        .minusDays(SUBMITTED_DAYS_PRIOR);

    var dateBeforeSubmitted = submittedDate.minusDays(1);

    nominationDecisionForm.getDecisionDate().setDate(dateBeforeSubmitted);

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors).extractingByKeys(
        "decisionDate.dayInput.inputValue",
        "decisionDate.monthInput.inputValue",
        "decisionDate.yearInput.inputValue"
    ).containsExactly(
        Set.of("Decision date must be the same as or after %s".formatted(
            DateUtil.formatShortDate(submittedDate))),
        Set.of(""),
        Set.of("")
    );
  }

  @Test
  void validate_whenDecisionDateAfterValidDate_thenVerifyError() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    var maxDecisionDate = LocalDate.ofInstant(clockNow.instant(), ZoneId.systemDefault());

    var decisionDate = maxDecisionDate.plusDays(1);

    nominationDecisionForm.getDecisionDate().setDate(decisionDate);

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors).extractingByKeys(
        "decisionDate.dayInput.inputValue",
        "decisionDate.monthInput.inputValue",
        "decisionDate.yearInput.inputValue"
    ).containsExactly(
        Set.of("Decision date must be the same as or before %s".formatted(
            DateUtil.formatShortDate(maxDecisionDate))),
        Set.of(""),
        Set.of("")
    );
  }

  @Test
  void validate_whenCommentsNotNull_thenVerifyNoError() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    nominationDecisionForm.getComments().setInputValue("comment text");

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);
    assertFalse(nominationDecisionForm.getComments().fieldHasErrors(bindingResult));
  }

  @Test
  void validate_whenFileHasNoDescription_thenVerifyHasError() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setUploadedFileId(UUID.randomUUID());
    uploadedFileForm.setUploadedFileInstant(Instant.now());
    uploadedFileForm.setFileName("document.%s".formatted(VALID_EXTENSION));

    nominationDecisionForm.setDecisionFiles(List.of(uploadedFileForm));

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors).contains(
        entry("decisionFiles[0].uploadedFileDescription", Set.of("Enter a description of this file"))
    );
  }

  @Test
  void validate_whenMoreThanOneFile_thenVerifyHasError() {
    var nominationDecisionForm = new NominationDecisionForm();
    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    var firstUploadedFileForm = new UploadedFileForm();
    firstUploadedFileForm.setUploadedFileId(UUID.randomUUID());
    firstUploadedFileForm.setUploadedFileInstant(Instant.now());

    var secondUploadedFileForm = new UploadedFileForm();
    secondUploadedFileForm.setUploadedFileId(UUID.randomUUID());
    secondUploadedFileForm.setUploadedFileInstant(Instant.now());

    nominationDecisionForm.setDecisionFiles(List.of(firstUploadedFileForm, secondUploadedFileForm));

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors).contains(
        entry("decisionFiles", Set.of("Only one decision document can be uploaded"))
    );
  }

  @Test
  void validate_whenFileExtensionIsUnsupported_thenVerifyErrors() {
    var nominationDecisionForm = new NominationDecisionForm();
    nominationDecisionForm.setNominationDecision(NominationDecision.NO_OBJECTION);
    nominationDecisionForm.getDecisionDate().setDate(LocalDate.ofInstant(clockNow.instant(), ZoneId.systemDefault()));
    nominationDecisionForm.getComments().setInputValue("comment text");

    var uploadedFile = UploadedFileTestUtil.builder()
        .withName("document.invalid-extension")
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());
    uploadedFileForm.setFileDescription(uploadedFile.getDescription());

    nominationDecisionForm.getDecisionFiles().add(uploadedFileForm);

    var bindingResult = new BeanPropertyBindingResult(nominationDecisionForm, "form");

    nominationDecisionValidator.validate(nominationDecisionForm, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    var allowedExtensions = "pdf";

    assertThat(errors)
        .containsExactly(
            entry("decisionFiles", Set.of(
                "The selected files must be a %s".formatted(allowedExtensions)
            ))
        );
  }

  @Test
  void supports_whenCorrectClass_thenSupports() {
    assertTrue(nominationDecisionValidator.supports(NominationDecisionForm.class));
  }

  @Test
  void supports_whenIncorrectClass_thenDoesNotSupport() {
    assertFalse(nominationDecisionValidator.supports(UnsupportedClass.class));
  }

  private static class UnsupportedClass {

  }
}