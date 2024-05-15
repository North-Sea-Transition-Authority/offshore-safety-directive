package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

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
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationConsultationResponseValidatorTest {

  private static final String VALID_EXTENSION = "pdf";
  private static final FileUploadProperties FILE_UPLOAD_PROPERTIES = FileUploadPropertiesTestUtil.builder()
      .withDefaultPermittedFileExtensions(Set.of("default-extension", VALID_EXTENSION))
      .build();

  private NominationConsultationResponseValidator nominationConsultationResponseValidator;

  @BeforeEach
  void setUp() {
    nominationConsultationResponseValidator = new NominationConsultationResponseValidator(FILE_UPLOAD_PROPERTIES);
  }

  @Test
  void supports_whenValid() {
    var isValid = nominationConsultationResponseValidator.supports(NominationConsultationResponseForm.class);
    assertTrue(isValid);
  }

  @Test
  void supports_whenInvalid() {
    var isValid = nominationConsultationResponseValidator.supports(UnsupportedClass.class);
    assertFalse(isValid);
  }

  @Test
  void validate_whenNoFieldsPopulated_thenVerifyHasErrors() {
    var form = new NominationConsultationResponseForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominationConsultationResponseValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "response.inputValue",
                "response.required",
                "Enter response"
            )
        );
  }

  @Test
  void validate_whenFullyPopulated_thenNoErrors() {
    var form = getValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominationConsultationResponseValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_whenFileUploaded_andNoDescription_thenVerifyHasError() {
    var form = getValidForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var uploadedFile = UploadedFileTestUtil.builder()
        .withName("document.%s".formatted(VALID_EXTENSION))
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());

    form.getConsultationResponseFiles().add(uploadedFileForm);

    nominationConsultationResponseValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "consultationResponseFiles[0].uploadedFileDescription",
                "consultationResponseFiles[0].uploadedFileDescription.required",
                "Enter a description of this file"
            )
        );
  }

  @Test
  void validate_whenFileUploaded_andHasDescription_thenVerifyNoError() {
    var form = getValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var uploadedFile = UploadedFileTestUtil.builder()
        .withName("document.%s".formatted(VALID_EXTENSION))
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());
    uploadedFileForm.setFileDescription(uploadedFile.getDescription());

    form.getConsultationResponseFiles().add(uploadedFileForm);

    nominationConsultationResponseValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_whenFileExtensionIsUnsupported_thenVerifyErrors() {
    var form = new NominationConsultationResponseForm();
    form.getResponse().setInputValue("response");

    var uploadedFile = UploadedFileTestUtil.builder()
        .withName("document.invalid-extension")
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());
    uploadedFileForm.setFileDescription(uploadedFile.getDescription());

    form.getConsultationResponseFiles().add(uploadedFileForm);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominationConsultationResponseValidator.validate(form, bindingResult);

    var allowedExtensions = FILE_UPLOAD_PROPERTIES.defaultPermittedFileExtensions()
        .stream()
        .sorted(String::compareToIgnoreCase)
        .collect(Collectors.joining(", "));

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "consultationResponseFiles",
                "consultationResponseFiles.invalidExtension",
                "The selected files must be a %s".formatted(allowedExtensions)
            )
        );
  }

  private NominationConsultationResponseForm getValidForm() {
    var form = new NominationConsultationResponseForm();
    form.getResponse().setInputValue("text");
    return form;
  }

  private static class UnsupportedClass {
  }

}