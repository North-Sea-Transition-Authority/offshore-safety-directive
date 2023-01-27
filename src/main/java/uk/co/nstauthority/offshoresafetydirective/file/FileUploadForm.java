package uk.co.nstauthority.offshoresafetydirective.file;

import java.time.Instant;
import java.util.UUID;

public class FileUploadForm {
  private UUID uploadedFileId;

  private String uploadedFileDescription;

  private Instant uploadedFileInstant;

  public UUID getUploadedFileId() {
    return uploadedFileId;
  }

  public void setUploadedFileId(UUID uploadedFileId) {
    this.uploadedFileId = uploadedFileId;
  }

  public String getUploadedFileDescription() {
    return uploadedFileDescription;
  }

  public void setUploadedFileDescription(String uploadedFileDescription) {
    this.uploadedFileDescription = uploadedFileDescription;
  }

  public Instant getUploadedFileInstant() {
    return uploadedFileInstant;
  }

  public void setUploadedFileInstant(Instant uploadedFileInstant) {
    this.uploadedFileInstant = uploadedFileInstant;
  }
}
