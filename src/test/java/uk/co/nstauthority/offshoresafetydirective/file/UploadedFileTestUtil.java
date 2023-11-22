package uk.co.nstauthority.offshoresafetydirective.file;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class UploadedFileTestUtil {

  private UploadedFileTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  // TODO OSDOP-457 - Rename newBuilder to builder once all reference are no longer in use
  public static NewBuilder newBuilder() {
    return new NewBuilder();
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

    public OldUploadedFile build() {
      var uploadedFile = new OldUploadedFile();
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


  // TODO OSDOP-457 - Rename NewBuilder to Builder
  public static class NewBuilder {

    private UUID id = UUID.randomUUID();
    private String bucket = "bucket-%s".formatted(UUID.randomUUID());
    private String key = "key-%s".formatted(UUID.randomUUID());
    private String usageId = "usage-id-%s".formatted(UUID.randomUUID());
    private String usageType = "usage-type-%s".formatted(UUID.randomUUID());
    private String documentType = "doc-type-%s".formatted(UUID.randomUUID());
    private String name = "doc-%s".formatted(UUID.randomUUID());
    private String contentType = "pdf";
    private long contentLength = new Random().nextLong(Long.MAX_VALUE);
    private Instant uploadedAt = Instant.now();
    private String uploadedBy = "uploaded-by-%s".formatted(UUID.randomUUID());
    private String description = "description-%s".formatted(UUID.randomUUID());

    private NewBuilder() {
    }

    public NewBuilder withId(UUID id) {
      this.id = id;
      return this;
    }

    public NewBuilder withBucket(String bucket) {
      this.bucket = bucket;
      return this;
    }

    public NewBuilder withKey(String key) {
      this.key = key;
      return this;
    }

    public NewBuilder withUsageId(String usageId) {
      this.usageId = usageId;
      return this;
    }

    public NewBuilder withUsageType(String usageType) {
      this.usageType = usageType;
      return this;
    }

    public NewBuilder withDocumentType(String documentType) {
      this.documentType = documentType;
      return this;
    }

    public NewBuilder withName(String name) {
      this.name = name;
      return this;
    }

    public NewBuilder withContentType(String contentType) {
      this.contentType = contentType;
      return this;
    }

    public NewBuilder withContentLength(long contentLength) {
      this.contentLength = contentLength;
      return this;
    }

    public NewBuilder withUploadedAt(Instant uploadedAt) {
      this.uploadedAt = uploadedAt;
      return this;
    }

    public NewBuilder withUploadedBy(String uploadedBy) {
      this.uploadedBy = uploadedBy;
      return this;
    }

    public NewBuilder withDescription(String description) {
      this.description = description;
      return this;
    }

    public UploadedFile build() {
      var file = new UploadedFile();
      file.setId(id);
      file.setBucket(bucket);
      file.setKey(key);
      file.setUsageId(usageId);
      file.setUsageType(usageType);
      file.setDocumentType(documentType);
      file.setName(name);
      file.setContentType(contentType);
      file.setContentLength(contentLength);
      file.setUploadedAt(uploadedAt);
      file.setUploadedBy(uploadedBy);
      file.setDescription(description);
      return file;
    }
  }
}