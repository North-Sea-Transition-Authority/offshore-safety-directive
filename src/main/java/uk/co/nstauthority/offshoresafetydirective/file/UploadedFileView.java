package uk.co.nstauthority.offshoresafetydirective.file;

import java.time.Instant;
import uk.co.nstauthority.offshoresafetydirective.fds.fileupload.FileUploadItem;

public record UploadedFileView(String fileId, String fileName, String fileSize, String fileDescription,
                               Instant fileUploadedTime) implements FileUploadItem {

  @Override
  public String getFileId() {
    return fileId;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  @Override
  public String getFileSize() {
    return fileSize;
  }

  @Override
  public String getFileDescription() {
    return fileDescription;
  }

  @Override
  public Instant getFileUploadedTime() {
    return fileUploadedTime;
  }
}
