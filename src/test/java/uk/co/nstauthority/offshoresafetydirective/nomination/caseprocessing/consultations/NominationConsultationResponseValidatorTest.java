package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class NominationConsultationResponseValidatorTest {

  @InjectMocks
  private NominationConsultationResponseValidator nominationConsultationResponseValidator;

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
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(UUID.randomUUID());
    form.getConsultationResponseFiles().add(fileUploadForm);
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
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(UUID.randomUUID());
    fileUploadForm.setUploadedFileDescription("description");
    form.getConsultationResponseFiles().add(fileUploadForm);
    nominationConsultationResponseValidator.validate(form, bindingResult);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errors)
        .doesNotContainKey("consultationResponseFiles[0].uploadedFileDescription");
  }

  private static class UnsupportedClass {
  }

}