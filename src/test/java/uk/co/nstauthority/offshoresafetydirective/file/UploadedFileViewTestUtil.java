package uk.co.nstauthority.offshoresafetydirective.file;

public class UploadedFileViewTestUtil {

  public static UploadedFileView fromUploadedFile(OldUploadedFile uploadedFile) {
    return new UploadedFileView(
        uploadedFile.getId().toString(),
        uploadedFile.getFilename(),
        String.valueOf(uploadedFile.getFileSizeBytes()),
        uploadedFile.getDescription(),
        uploadedFile.getUploadedTimeStamp()
    );
  }

}
