package uk.co.nstauthority.offshoresafetydirective.file;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class FileUploadResultTest {

  private static final String FILE_ID = "fileId";
  private static final String NAME = "file";
  private static final String ORIGINAL_FILENAME = "document.pdf";
  private static final String CONTENT_TYPE = MediaType.APPLICATION_PDF_VALUE;
  private static final byte[] FILE_CONTENT = new byte[]{1, 2, 3, 4, 5};

  private MultipartFile file;

  @BeforeEach
  void setUp() {
    this.file = new MockMultipartFile(NAME, ORIGINAL_FILENAME, CONTENT_TYPE, FILE_CONTENT);
  }

  @ParameterizedTest
  @EnumSource(UploadErrorType.class)
  void error(UploadErrorType uploadErrorType) {
    var result = FileUploadResult.error(ORIGINAL_FILENAME, file, uploadErrorType);

    assertThat(result.getFileId()).isEmpty();
    assertThat(result.getFileName()).isEqualTo(ORIGINAL_FILENAME);
    assertThat(result.getSize()).isEqualTo(file.getSize());
    assertThat(result.getContentType()).isEqualTo(CONTENT_TYPE);
    assertThat(result.getErrorType()).isPresent() .get().isEqualTo(uploadErrorType);
    assertThat(result.getErrorMessage()).isPresent().get().isEqualTo(uploadErrorType.getErrorMessage());
    assertThat(result.isValid()).isFalse();
  }

  @Test
  void valid() {
    var result = FileUploadResult.valid(FILE_ID, ORIGINAL_FILENAME, file);

    assertThat(result.getFileId()).isPresent().get().isEqualTo(FILE_ID);
    assertThat(result.getFileName()).isEqualTo(ORIGINAL_FILENAME);
    assertThat(result.getSize()).isEqualTo(file.getSize());
    assertThat(result.getContentType()).isEqualTo(CONTENT_TYPE);
    assertThat(result.getErrorType()).isEmpty();
    assertThat(result.getErrorMessage()).isEmpty();
    assertThat(result.isValid()).isTrue();
  }
}