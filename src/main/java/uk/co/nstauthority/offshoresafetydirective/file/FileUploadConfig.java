package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadConfig {

  private final Integer maxFileUploadBytes;
  private final Set<String> defaultPermittedFileExtensions;

  @ConstructorBinding
  FileUploadConfig(Integer maxFileUploadBytes, String allowedFileExtensionsCsv) {
    this.maxFileUploadBytes = maxFileUploadBytes;
    this.defaultPermittedFileExtensions = Arrays.stream(allowedFileExtensionsCsv.split(",")).collect(Collectors.toSet());
  }

  FileUploadConfig(Integer maxFileUploadBytes, Set<String> defaultPermittedFileExtensions) {
    this.maxFileUploadBytes = maxFileUploadBytes;
    this.defaultPermittedFileExtensions = defaultPermittedFileExtensions;
  }

  public Integer getMaxFileUploadBytes() {
    return maxFileUploadBytes;
  }

  public Set<String> getDefaultPermittedFileExtensions() {
    return defaultPermittedFileExtensions;
  }

}