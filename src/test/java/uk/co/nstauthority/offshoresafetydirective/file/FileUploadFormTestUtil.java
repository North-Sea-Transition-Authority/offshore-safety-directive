package uk.co.nstauthority.offshoresafetydirective.file;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class FileUploadFormTestUtil {

  private FileUploadFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID uploadedFileId = UUID.randomUUID();
    private String uploadedFileDescription = "description";
    private Instant uploadedFileInstant = Instant.now();

    public Builder() {
    }

    public Builder withUploadedFileId(UUID uploadedFileId) {
      this.uploadedFileId = uploadedFileId;
      return this;
    }

    public Builder withUploadedFileDescription(String uploadedFileDescription) {
      this.uploadedFileDescription = uploadedFileDescription;
      return this;
    }

    public Builder withUploadedFileInstant(Instant uploadedFileInstant) {
      this.uploadedFileInstant = uploadedFileInstant;
      return this;
    }

    public FileUploadForm build() {
      var fileUploadForm = new FileUploadForm();
      fileUploadForm.setUploadedFileId(uploadedFileId);
      fileUploadForm.setUploadedFileInstant(uploadedFileInstant);
      fileUploadForm.setUploadedFileDescription(uploadedFileDescription);
      return fileUploadForm;
    }
  }
}