package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadConfig {

  private final Integer maxFileUploadBytes;
  private final List<String> allowedFileExtensions;
  private final String filenameDisallowedCharactersRegex;

  @ConstructorBinding
  FileUploadConfig(Integer maxFileUploadBytes, String allowedFileExtensionsCsv,
                   String filenameDisallowedCharactersRegex) {

    this.maxFileUploadBytes = maxFileUploadBytes;
    this.allowedFileExtensions = Arrays.stream(allowedFileExtensionsCsv.split(",")).toList();
    this.filenameDisallowedCharactersRegex = filenameDisallowedCharactersRegex;
  }

  FileUploadConfig(Integer maxFileUploadBytes, List<String> allowedFileExtensions,
                   String filenameDisallowedCharactersRegex) {

    this.maxFileUploadBytes = maxFileUploadBytes;
    this.allowedFileExtensions = allowedFileExtensions;
    this.filenameDisallowedCharactersRegex = filenameDisallowedCharactersRegex;
  }

  public Integer getMaxFileUploadBytes() {
    return maxFileUploadBytes;
  }

  public List<String> getAllowedFileExtensions() {
    return allowedFileExtensions;
  }

  public String getFilenameDisallowedCharactersRegex() {
    return filenameDisallowedCharactersRegex;
  }
}