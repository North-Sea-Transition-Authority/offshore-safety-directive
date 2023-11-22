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
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

class FileValidationUtilTest {

  private static final String VALID_EXTENSION = "valid-extension";

  @Test
  void validator_valid() {
    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setUploadedFileId(UUID.randomUUID());
    uploadedFileForm.setUploadedFileInstant(Instant.now());
    uploadedFileForm.setUploadedFileDescription("description");
    uploadedFileForm.setFileName("file.%s".formatted(VALID_EXTENSION));

    var form = new FileForm(List.of(uploadedFileForm));
    var errors = new BeanPropertyBindingResult(form, "form");

    FileValidationUtil.validator()
        .withMinimumNumberOfFiles(1, null)
        .withMaximumNumberOfFiles(1, null)
        .validate(errors, form.getFiles(), "files", Set.of(VALID_EXTENSION));

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
        .validate(errors, form.getFiles(), "files", Set.of(VALID_EXTENSION));

    var errorValues = ValidatorTestingUtil.extractErrorMessages(errors);
    assertThat(errorValues)
        .containsExactly(entry("files", Set.of(minErrorMessage)));
  }

  @Test
  void validator_moreThanMax() {
    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setUploadedFileId(UUID.randomUUID());
    uploadedFileForm.setUploadedFileInstant(Instant.now());
    uploadedFileForm.setUploadedFileDescription("description");
    uploadedFileForm.setFileName("file.%s".formatted(VALID_EXTENSION));

    var form = new FileForm(List.of(uploadedFileForm, uploadedFileForm));
    var errors = new BeanPropertyBindingResult(form, "form");
    var maxErrorMessage = "max message";

    FileValidationUtil.validator()
        .withMinimumNumberOfFiles(1, null)
        .withMaximumNumberOfFiles(1, maxErrorMessage)
        .validate(errors, form.getFiles(), "files", Set.of(VALID_EXTENSION));

    var errorValues = ValidatorTestingUtil.extractErrorMessages(errors);
    assertThat(errorValues)
        .containsExactly(entry("files", Set.of(maxErrorMessage)));
  }

  @Test
  void validator_invalidExtension() {
    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setUploadedFileId(UUID.randomUUID());
    uploadedFileForm.setUploadedFileInstant(Instant.now());
    uploadedFileForm.setUploadedFileDescription("description");
    uploadedFileForm.setFileName("file.invalid-extension");

    var form = new FileForm(List.of(uploadedFileForm));
    var errors = new BeanPropertyBindingResult(form, "form");

    var otherValidExtension = "other-valid";

    FileValidationUtil.validator()
        .withMinimumNumberOfFiles(1, null)
        .withMaximumNumberOfFiles(1, null)
        .validate(errors, form.getFiles(), "files", Set.of(VALID_EXTENSION, otherValidExtension));

    var errorValues = ValidatorTestingUtil.extractErrorMessages(errors);
    assertThat(errorValues)
        .containsExactly(entry("files", Set.of(
            "The selected files must be a %s, %s".formatted(otherValidExtension, VALID_EXTENSION)
        )));
  }

  public static class FileForm {
    private final List<UploadedFileForm> files;

    public FileForm(List<UploadedFileForm> files) {
      this.files = files;
    }

    public List<UploadedFileForm> getFiles() {
      return files;
    }
  }
}