package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.ArrayList;
import java.util.List;
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
    private List<String> allowedFileExtensions = new ArrayList<>();
    private String filenameDisallowedCharactersRegex = null;

    private Builder() {
    }

    public Builder withMaxFileUploadBytes(Integer maxFileUploadBytes) {
      this.maxFileUploadBytes = maxFileUploadBytes;
      return this;
    }

    public Builder withAllowedFileExtensions(List<String> extensions) {
      this.allowedFileExtensions = extensions;
      return this;
    }

    public Builder withAllowedFileExtension(String extension) {
      this.allowedFileExtensions.add(extension);
      return this;
    }

    public Builder withFilenameDisallowedCharactersRegex(String filenameDisallowedCharactersRegex) {
      this.filenameDisallowedCharactersRegex = filenameDisallowedCharactersRegex;
      return this;
    }

    public FileUploadConfig build() {
      return new FileUploadConfig(maxFileUploadBytes, allowedFileExtensions, filenameDisallowedCharactersRegex);
    }
  }
}