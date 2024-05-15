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

  public static class Builder {

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

    private Builder() {
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withBucket(String bucket) {
      this.bucket = bucket;
      return this;
    }

    public Builder withKey(String key) {
      this.key = key;
      return this;
    }

    public Builder withUsageId(String usageId) {
      this.usageId = usageId;
      return this;
    }

    public Builder withUsageType(String usageType) {
      this.usageType = usageType;
      return this;
    }

    public Builder withDocumentType(String documentType) {
      this.documentType = documentType;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withContentType(String contentType) {
      this.contentType = contentType;
      return this;
    }

    public Builder withContentLength(long contentLength) {
      this.contentLength = contentLength;
      return this;
    }

    public Builder withUploadedAt(Instant uploadedAt) {
      this.uploadedAt = uploadedAt;
      return this;
    }

    public Builder withUploadedBy(String uploadedBy) {
      this.uploadedBy = uploadedBy;
      return this;
    }

    public Builder withDescription(String description) {
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