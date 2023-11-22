package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

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
    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors)
        .containsExactlyInAnyOrderEntriesOf(
            Map.of("response.inputValue", Set.of("Enter response"))
        );
  }

  @Test
  void validate_whenFullyPopulated_thenNoErrors() {
    var form = new NominationConsultationResponseForm();
    form.getResponse().setInputValue("text");
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    nominationConsultationResponseValidator.validate(form, bindingResult);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenFileUploaded_andNoDescription_thenVerifyHasError() {
    var form = new NominationConsultationResponseForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var uploadedFile = UploadedFileTestUtil.newBuilder()
        .withName("document.%s".formatted(VALID_EXTENSION))
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());

    form.getConsultationResponseFiles().add(uploadedFileForm);

    nominationConsultationResponseValidator.validate(form, bindingResult);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors)
        .containsEntry(
            "consultationResponseFiles[0].uploadedFileDescription", Set.of("Enter a description of this file")
        );
  }

  @Test
  void validate_whenFileUploaded_andHasDescription_thenVerifyNoError() {
    var form = new NominationConsultationResponseForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var uploadedFile = UploadedFileTestUtil.newBuilder()
        .withName("document.%s".formatted(VALID_EXTENSION))
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());
    uploadedFileForm.setFileDescription(uploadedFile.getDescription());

    form.getConsultationResponseFiles().add(uploadedFileForm);

    nominationConsultationResponseValidator.validate(form, bindingResult);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors)
        .doesNotContainKey("consultationResponseFiles[0].uploadedFileDescription");
  }

  @Test
  void validate_whenFileExtensionIsUnsupported_thenVerifyErrors() {
    var form = new NominationConsultationResponseForm();
    form.getResponse().setInputValue("response");

    var uploadedFile = UploadedFileTestUtil.newBuilder()
        .withName("document.invalid-extension")
        .build();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(uploadedFile.getId());
    uploadedFileForm.setFileName(uploadedFile.getName());
    uploadedFileForm.setFileDescription(uploadedFile.getDescription());

    form.getConsultationResponseFiles().add(uploadedFileForm);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominationConsultationResponseValidator.validate(form, bindingResult);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    var allowedExtensions = String.join(", ", FILE_UPLOAD_PROPERTIES.defaultPermittedFileExtensions());

    assertThat(errors)
        .containsExactly(
            entry("consultationResponseFiles", Set.of(
                "The selected files must be a %s".formatted(allowedExtensions)
            ))
        );
  }

  private static class UnsupportedClass {
  }

}