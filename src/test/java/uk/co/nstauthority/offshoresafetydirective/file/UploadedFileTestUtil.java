package uk.co.nstauthority.offshoresafetydirective.file;

import java.time.Instant;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class UploadedFileTestUtil {

  private UploadedFileTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private String s3Key = "s3 key";
    private String bucketName = "bucket name";
    private VirtualFolder virtualFolder = VirtualFolder.NOMINATION_DECISION;
    private String filename = "file name";
    private String fileContentType = "file content";
    private long fileSizeBytes = 100L;
    private Instant uploadedTimeStamp = Instant.now();
    private String description = "file description";

    private Builder() {
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withS3Key(String s3Key) {
      this.s3Key = s3Key;
      return this;
    }

    public Builder withBucketName(String bucketName) {
      this.bucketName = bucketName;
      return this;
    }

    public Builder withVirtualFolder(VirtualFolder virtualFolder) {
      this.virtualFolder = virtualFolder;
      return this;
    }

    public Builder withFilename(String filename) {
      this.filename = filename;
      return this;
    }

    public Builder withFileContentType(String fileContentType) {
      this.fileContentType = fileContentType;
      return this;
    }

    public Builder withFileSizeBytes(long fileSizeBytes) {
      this.fileSizeBytes = fileSizeBytes;
      return this;
    }

    public Builder withUploadedTimeStamp(Instant uploadedTimeStamp) {
      this.uploadedTimeStamp = uploadedTimeStamp;
      return this;
    }

    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    public UploadedFile build() {
      var uploadedFile = new UploadedFile();
      uploadedFile.setId(id);
      uploadedFile.setFileKey(s3Key);
      uploadedFile.setBucketName(bucketName);
      uploadedFile.setVirtualFolder(virtualFolder);
      uploadedFile.setFilename(filename);
      uploadedFile.setFileContentType(fileContentType);
      uploadedFile.setFileSizeBytes(fileSizeBytes);
      uploadedFile.setUploadedTimeStamp(uploadedTimeStamp);
      uploadedFile.setDescription(description);
      return uploadedFile;
    }
  }
}