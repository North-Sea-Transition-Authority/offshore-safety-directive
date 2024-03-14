package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;

@ExtendWith(MockitoExtension.class)
class GeneralCaseNoteValidatorTest {

  private static final String VALID_EXTENSION = "valid-extension";
  private static final FileUploadProperties FILE_UPLOAD_PROPERTIES = FileUploadPropertiesTestUtil.builder()
      .withDefaultPermittedFileExtensions(Set.of("default-extension", VALID_EXTENSION))
      .build();
  private GeneralCaseNoteValidator generalCaseNoteValidator;

  @BeforeEach
  void setUp() {
    generalCaseNoteValidator = new GeneralCaseNoteValidator(FILE_UPLOAD_PROPERTIES);
  }

  @Test
  void validate_whenFormIsValid_thenVerifyNoErrors() {
    var form = new GeneralCaseNoteForm();
    form.getCaseNoteSubject().setInputValue("Subject");
    form.getCaseNoteText().setInputValue("Case note body text");

    var uploadedFile = UploadedFileTestUtil.builder()
        .withName("document.%s".formatted(VALID_EXTENSION))
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());
    uploadedFileForm.setFileDescription(uploadedFile.getDescription());

    form.getCaseNoteFiles().add(uploadedFileForm);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    generalCaseNoteValidator.validate(form, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenFormIsValid_andHasNoDocuments_thenVerifyNoErrors() {
    var form = new GeneralCaseNoteForm();
    form.getCaseNoteSubject().setInputValue("Subject");
    form.getCaseNoteText().setInputValue("Case note body text");

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    generalCaseNoteValidator.validate(form, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenAllFieldsEmpty_thenVerifyErrors() {
    var form = new GeneralCaseNoteForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    generalCaseNoteValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("caseNoteSubject.inputValue", "caseNoteSubject.required", "Enter a case note subject"),
            tuple("caseNoteText.inputValue", "caseNoteText.required", "Enter case note text")
        );
  }

  @Test
  void validate_whenFileExtensionIsUnsupported_thenVerifyErrors() {
    var form = new GeneralCaseNoteForm();
    form.getCaseNoteSubject().setInputValue("Subject");
    form.getCaseNoteText().setInputValue("Case note body text");

    var uploadedFile = UploadedFileTestUtil.builder()
        .withName("document.invalid-extension")
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());
    uploadedFileForm.setFileDescription(uploadedFile.getDescription());

    form.getCaseNoteFiles().add(uploadedFileForm);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    generalCaseNoteValidator.validate(form, bindingResult);

    var allowedExtensions = FILE_UPLOAD_PROPERTIES.defaultPermittedFileExtensions()
        .stream()
        .sorted(String::compareToIgnoreCase)
        .collect(Collectors.joining(", "));

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("caseNoteFiles", "caseNoteFiles.invalidExtension", "The selected files must be a %s".formatted(allowedExtensions))
        );
  }


  @Test
  void supports_unsupportedClass() {
    assertFalse(generalCaseNoteValidator.supports(UnsupportedClass.class));
  }

  @Test
  void supports_supportedClass() {
    assertTrue(generalCaseNoteValidator.supports(GeneralCaseNoteForm.class));
  }

  private static class UnsupportedClass {
  }
}