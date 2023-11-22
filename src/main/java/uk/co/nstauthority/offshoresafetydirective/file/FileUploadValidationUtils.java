package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.List;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

public class FileUploadValidationUtils {

  private FileUploadValidationUtils() {
    throw new AssertionError();
  }

  // TODO OSDOP-457 - Remove this once all usages are removed
  public static void rejectIfFileDescriptionsAreEmptyOrWhitespace(Errors errors, List<FileUploadForm> fileUploadForms,
                                                                  String field) {
    int bound = fileUploadForms.size();
    for (int i = 0; i < bound; i++) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "%s[%d].uploadedFileDescription".formatted(field, i),
          "%s[%d].uploadedFileDescription.required".formatted(field, i), "Enter a description of this file");
    }
  }

  public static void rejectIfUploadedFileFormDescriptionsAreEmptyOrWhitespace(Errors errors,
                                                                              List<UploadedFileForm> fileUploadForms,
                                                                              String field) {
    int bound = fileUploadForms.size();
    for (int i = 0; i < bound; i++) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "%s[%d].uploadedFileDescription".formatted(field, i),
          "%s[%d].uploadedFileDescription.required".formatted(field, i), "Enter a description of this file");
    }
  }
}
