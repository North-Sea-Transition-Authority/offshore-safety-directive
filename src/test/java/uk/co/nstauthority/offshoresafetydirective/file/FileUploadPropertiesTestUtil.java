package uk.co.nstauthority.offshoresafetydirective.file;

import java.time.Duration;
import java.util.Set;
import org.springframework.util.unit.DataSize;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class FileUploadPropertiesTestUtil {

  private FileUploadPropertiesTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private FileUploadProperties.S3 s3;
    private FileUploadProperties.ClamAv clamAv;
    private Duration orphanFileTtl;
    private DataSize defaultMaximumFileSize;
    private Set<String> defaultPermittedFileExtensions;

    private Builder() {
    }

    public Builder withS3(FileUploadProperties.S3 s3) {
      this.s3 = s3;
      return this;
    }

    public Builder withClamAv(FileUploadProperties.ClamAv clamAv) {
      this.clamAv = clamAv;
      return this;
    }

    public Builder withOrphanFileTtl(Duration orphanFileTtl) {
      this.orphanFileTtl = orphanFileTtl;
      return this;
    }

    public Builder withDefaultMaximumFileSize(DataSize defaultMaximumFileSize) {
      this.defaultMaximumFileSize = defaultMaximumFileSize;
      return this;
    }

    public Builder withDefaultPermittedFileExtensions(Set<String> defaultPermittedFileExtensions) {
      this.defaultPermittedFileExtensions = defaultPermittedFileExtensions;
      return this;
    }

    public FileUploadProperties build() {
      return new FileUploadProperties(
          s3,
          clamAv,
          orphanFileTtl,
          defaultMaximumFileSize,
          defaultPermittedFileExtensions
      );
    }
  }
}
