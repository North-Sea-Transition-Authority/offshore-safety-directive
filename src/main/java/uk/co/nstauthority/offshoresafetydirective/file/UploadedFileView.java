package uk.co.nstauthority.offshoresafetydirective.file;

import java.time.Instant;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.fds.fileupload.FileUploadItem;

public record UploadedFileView(String fileId, String fileName, String fileSize, String fileDescription,
                               Instant fileUploadedTime) implements FileUploadItem {

  public static UploadedFileView from(UploadedFile uploadedFile) {
    return new UploadedFileView(
        uploadedFile.getId().toString(),
        uploadedFile.getName(),
        FileUploadLibraryUtils.formatSize(uploadedFile.getContentLength()),
        uploadedFile.getDescription(),
        uploadedFile.getUploadedAt()
    );
  }

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
