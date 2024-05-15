package uk.co.nstauthority.offshoresafetydirective.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;

class UploadedFileViewTest {

  @Test
  void from_verifyMapping() {
    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(UUID.randomUUID())
        .withName("file name")
        .withContentLength(100)
        .withDescription("file description")
        .withUploadedAt(Instant.now())
        .build();
    var uploadedFileView = UploadedFileView.from(uploadedFile);

    assertThat(uploadedFileView)
        .extracting(
            UploadedFileView::fileId,
            UploadedFileView::fileName,
            UploadedFileView::fileSize,
            UploadedFileView::fileDescription,
            UploadedFileView::fileUploadedTime
        )
        .containsExactly(
            uploadedFile.getId().toString(),
            uploadedFile.getName(),
            FileUploadLibraryUtils.formatSize(uploadedFile.getContentLength()),
            uploadedFile.getDescription(),
            uploadedFile.getUploadedAt()
        );
  }
}