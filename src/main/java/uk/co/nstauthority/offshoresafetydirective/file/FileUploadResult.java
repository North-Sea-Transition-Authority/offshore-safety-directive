package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Optional;
import javax.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;

public class FileUploadResult {

  @Nullable
  private final String fileId;
  private final String fileName;
  private final Long size;
  private final String contentType;
  @Nullable
  private final UploadErrorType errorType;

  public FileUploadResult(@Nullable String fileId, String fileName, MultipartFile multipartFile,
                           @Nullable UploadErrorType errorType) {
    this.fileId = fileId;
    this.fileName = fileName;
    this.size = multipartFile.getSize();
    this.contentType = multipartFile.getContentType();
    this.errorType = errorType;
  }

  public static FileUploadResult error(String fileName, MultipartFile multipartFile, UploadErrorType errorType) {
    return new FileUploadResult(null, fileName, multipartFile, errorType);
  }

  public static FileUploadResult valid(String fileId, String fileName, MultipartFile multipartFile) {
    return new FileUploadResult(fileId, fileName, multipartFile, null);
  }

  public Optional<String> getFileId() {
    return Optional.ofNullable(fileId);
  }

  public String getFileName() {
    return fileName;
  }

  public Long getSize() {
    return size;
  }

  public String getContentType() {
    return contentType;
  }

  public Optional<UploadErrorType> getErrorType() {
    return Optional.ofNullable(errorType);
  }

  public Optional<String> getErrorMessage() {
    return getErrorType().map(UploadErrorType::getErrorMessage);
  }

  public boolean isValid() {
    return this.errorType == null && this.fileId != null;
  }
}

