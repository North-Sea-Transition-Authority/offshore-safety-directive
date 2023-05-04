package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import java.time.Instant;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;

public class UploadedFileDetailTestUtil {

  private UploadedFileDetailTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID uuid = UUID.randomUUID();
    private UploadedFile uploadedFile = UploadedFileTestUtil.builder().build();
    private FileStatus fileStatus = FileStatus.DRAFT;
    private FileReferenceType referenceType = FileReferenceType.NOMINATION_DETAIL;
    private String referenceId = "reference id";
    private String purpose = "purpose";
    private Instant uploadedInstant = Instant.now();

    private Builder() {

    }

    public Builder withUuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder withUploadedFile(UploadedFile uploadedFile) {
      this.uploadedFile = uploadedFile;
      return this;
    }

    public Builder withFileStatus(FileStatus fileStatus) {
      this.fileStatus = fileStatus;
      return this;
    }

    public Builder withReferenceType(
        FileReferenceType referenceType) {
      this.referenceType = referenceType;
      return this;
    }

    public Builder withReferenceId(String referenceId) {
      this.referenceId = referenceId;
      return this;
    }

    public Builder withPurpose(String purpose) {
      this.purpose = purpose;
      return this;
    }

    public Builder withUploadedInstant(Instant uploadedInstant) {
      this.uploadedInstant = uploadedInstant;
      return this;
    }

    public UploadedFileDetail build() {
      var fileDetail = new UploadedFileDetail(uuid);
      fileDetail.setUploadedFile(uploadedFile);
      fileDetail.setFileStatus(fileStatus);
      fileDetail.setReferenceType(referenceType);
      fileDetail.setReferenceId(referenceId);
      fileDetail.setPurpose(purpose);
      fileDetail.setUploadedInstant(uploadedInstant);
      return fileDetail;
    }
  }
}