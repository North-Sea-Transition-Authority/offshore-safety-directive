package uk.co.nstauthority.offshoresafetydirective.validationutil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadValidationUtils;

public class FileValidationUtil {

  public static final String FILES_EMPTY_ERROR_CODE = "%s.belowThreshold";
  public static final String FILES_TOO_MANY_ERROR_CODE = "%s.limitExceeded";
  public static final String FILES_INVALID_EXTENSION_ERROR_CODE = "%s.invalidExtension";

  private FileValidationUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Validator validator() {
    return new Validator();
  }

  public static class Validator {

    private int maximumNumberOfFiles = Integer.MAX_VALUE;
    private String maxErrorMessage;

    private int minimumNumberOfFiles = 0;
    private String minErrorMessage;

    private Validator() {
    }

    public Validator withMaximumNumberOfFiles(int maxFileCount, String errorMessage) {
      this.maximumNumberOfFiles = maxFileCount;
      this.maxErrorMessage = errorMessage;
      return this;
    }

    public Validator withMinimumNumberOfFiles(int minFileCount, String errorMessage) {
      this.minimumNumberOfFiles = minFileCount;
      this.minErrorMessage = errorMessage;
      return this;
    }

    public void validate(Errors errors, List<FileUploadForm> fileUploadForms, String fieldName) {

      if (minimumNumberOfFiles > 0 && CollectionUtils.isEmpty(fileUploadForms)) {
        errors.rejectValue(
            fieldName,
            FILES_EMPTY_ERROR_CODE.formatted(fieldName),
            minErrorMessage
        );
        return;
      }

      if (fileUploadForms != null && fileUploadForms.size() > maximumNumberOfFiles) {
        errors.rejectValue(
            fieldName,
            FILES_TOO_MANY_ERROR_CODE.formatted(fieldName),
            maxErrorMessage
        );
        return;
      }

      FileUploadValidationUtils.rejectIfFileDescriptionsAreEmptyOrWhitespace(
          errors,
          Objects.requireNonNull(fileUploadForms),
          fieldName
      );
    }

    // TODO OSDOP-457 - Replace original validate method with this
    public void validate2(Errors errors, List<UploadedFileForm> fileUploadForms, String fieldName,
                          Set<String> allowedExtensions) {

      if (minimumNumberOfFiles > 0 && CollectionUtils.isEmpty(fileUploadForms)) {
        errors.rejectValue(
            fieldName,
            FILES_EMPTY_ERROR_CODE.formatted(fieldName),
            minErrorMessage
        );
        return;
      } else if (fileUploadForms != null && fileUploadForms.size() > maximumNumberOfFiles) {
        errors.rejectValue(
            fieldName,
            FILES_TOO_MANY_ERROR_CODE.formatted(fieldName),
            maxErrorMessage
        );
        return;
      } else if (!CollectionUtils.isEmpty(fileUploadForms)) {
        for (UploadedFileForm file : fileUploadForms) {

          var fileExtension = Arrays.stream(file.getFileName()
              .split("\\."))
              .reduce((a, b) -> b);

          var fileHasAllowedExtension = allowedExtensions.stream()
              .anyMatch(s -> fileExtension.isPresent() && fileExtension.get().equalsIgnoreCase(s));

          if (!fileHasAllowedExtension) {
            var allowedExtensionsString = String.join(", ", allowedExtensions);
            errors.rejectValue(
                fieldName,
                FILES_INVALID_EXTENSION_ERROR_CODE.formatted(fieldName),
                "The selected files must be a %s".formatted(allowedExtensionsString)
            );
            return;
          }

        }
      }

      FileUploadValidationUtils.rejectIfUploadedFileFormDescriptionsAreEmptyOrWhitespace(
          errors,
          Objects.requireNonNull(fileUploadForms),
          fieldName
      );
    }

  }
}
