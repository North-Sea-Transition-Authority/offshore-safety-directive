package uk.co.nstauthority.offshoresafetydirective.file;

import java.time.Instant;
import java.util.UUID;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class UploadedFileFormTestUtil {


  public static final String VALID_FILE_EXTENSION = "valid-extension";

  private UploadedFileFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID fileId = UUID.randomUUID();
    private String fileName = "name-%s.%s".formatted(UUID.randomUUID(), VALID_FILE_EXTENSION);
    private String fileSize = FileUploadLibraryUtils.formatSize(100);
    private String fileDescription = "description-%s".formatted(UUID.randomUUID());
    private Instant fileUploadedAt = Instant.now();

    private Builder() {
    }

    public Builder withFileId(UUID fileId) {
      this.fileId = fileId;
      return this;
    }

    public Builder withFileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    public Builder withFileSize(String fileSize) {
      this.fileSize = fileSize;
      return this;
    }

    public Builder withFileDescription(String fileDescription) {
      this.fileDescription = fileDescription;
      return this;
    }

    public Builder withFileUploadedAt(Instant fileUploadedAt) {
      this.fileUploadedAt = fileUploadedAt;
      return this;
    }

    public UploadedFileForm build() {
      var form = new UploadedFileForm();
      form.setFileId(fileId);
      form.setFileName(fileName);
      form.setFileSize(fileSize);
      form.setFileDescription(fileDescription);
      form.setFileUploadedAt(fileUploadedAt);
      return form;
    }
  }
}
