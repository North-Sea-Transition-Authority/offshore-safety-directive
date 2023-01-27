package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class NominationDetailFileTestUtil {

  private NominationDetailFileTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID uuid = UUID.randomUUID();
    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();
    private UploadedFile uploadedFile = UploadedFileTestUtil.builder().build();
    private FileStatus fileStatus = FileStatus.DRAFT;

    private Builder() {
    }

    public Builder withUuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder withNominationDetail(
        NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
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

    public NominationDetailFile build() {
      var nominationDetailFile = new NominationDetailFile(uuid);
      nominationDetailFile.setNominationDetail(nominationDetail);
      nominationDetailFile.setUploadedFile(uploadedFile);
      nominationDetailFile.setFileStatus(fileStatus);
      return nominationDetailFile;
    }
  }
}