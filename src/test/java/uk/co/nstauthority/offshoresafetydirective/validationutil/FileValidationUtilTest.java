package uk.co.nstauthority.offshoresafetydirective.validationutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

class FileValidationUtilTest {

  @Test
  void validator_valid() {
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(UUID.randomUUID());
    fileUploadForm.setUploadedFileInstant(Instant.now());
    fileUploadForm.setUploadedFileDescription("description");

    var form = new FileForm(List.of(fileUploadForm));
    var errors = new BeanPropertyBindingResult(form, "form");

    FileValidationUtil.validator()
        .withMinimumNumberOfFiles(1, null)
        .withMaximumNumberOfFiles(1, null)
        .validate(errors, form.files, "files");

    assertFalse(errors.hasErrors());
  }

  @Test
  void validator_lessThanMin() {

    var form = new FileForm(List.of());
    var errors = new BeanPropertyBindingResult(form, "form");
    var minErrorMessage = "min message";

    FileValidationUtil.validator()
        .withMinimumNumberOfFiles(1, minErrorMessage)
        .withMaximumNumberOfFiles(1, null)
        .validate(errors, form.files, "files");

    var errorValues = ValidatorTestingUtil.extractErrorMessages(errors);
    assertThat(errorValues)
        .containsExactly(entry("files", Set.of(minErrorMessage)));
  }

  @Test
  void validator_moreThanMax() {
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(UUID.randomUUID());
    fileUploadForm.setUploadedFileInstant(Instant.now());
    fileUploadForm.setUploadedFileDescription("description");

    var form = new FileForm(List.of(fileUploadForm, fileUploadForm));
    var errors = new BeanPropertyBindingResult(form, "form");
    var maxErrorMessage = "max message";

    FileValidationUtil.validator()
        .withMinimumNumberOfFiles(1, null)
        .withMaximumNumberOfFiles(1, maxErrorMessage)
        .validate(errors, form.files, "files");

    var errorValues = ValidatorTestingUtil.extractErrorMessages(errors);
    assertThat(errorValues)
        .containsExactly(entry("files", Set.of(maxErrorMessage)));
  }

  public static class FileForm {
    private final List<FileUploadForm> files;

    public FileForm(List<FileUploadForm> files) {
      this.files = files;
    }

    public List<FileUploadForm> getFiles() {
      return files;
    }
  }
}