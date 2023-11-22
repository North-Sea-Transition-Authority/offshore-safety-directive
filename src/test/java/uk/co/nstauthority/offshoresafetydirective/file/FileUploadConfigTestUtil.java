package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.HashSet;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class FileUploadConfigTestUtil {

  private FileUploadConfigTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer maxFileUploadBytes = 52428800;
    private Set<String> allowedFileExtensions = new HashSet<>();

    private Builder() {
    }

    public Builder withMaxFileUploadBytes(Integer maxFileUploadBytes) {
      this.maxFileUploadBytes = maxFileUploadBytes;
      return this;
    }

    public Builder withAllowedFileExtensions(Set<String> extensions) {
      this.allowedFileExtensions = extensions;
      return this;
    }

    public Builder withAllowedFileExtension(String extension) {
      this.allowedFileExtensions.add(extension);
      return this;
    }

    public FileUploadConfig build() {
      return new FileUploadConfig(maxFileUploadBytes, allowedFileExtensions);
    }
  }
}