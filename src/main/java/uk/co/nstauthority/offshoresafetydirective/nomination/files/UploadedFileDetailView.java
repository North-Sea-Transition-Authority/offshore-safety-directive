package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;

public record UploadedFileDetailView(
    UploadedFileId uploadedFileId,
    String referenceId
) {

  public static UploadedFileDetailView from(UploadedFileDetail uploadedFileDetail) {
    return new UploadedFileDetailView(
        new UploadedFileId(uploadedFileDetail.getUploadedFile().getId()),
        uploadedFileDetail.getReferenceId()
    );
  }

}
