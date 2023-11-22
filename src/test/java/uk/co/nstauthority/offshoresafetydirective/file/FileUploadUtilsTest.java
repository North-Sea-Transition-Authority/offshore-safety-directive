package uk.co.nstauthority.offshoresafetydirective.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.net.HttpHeaders;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class FileUploadUtilsTest {

  @Test
  void fileSizeFormatter() {
    Map<Long, String> DATA_MAP = new HashMap<>() {{
      put(0L, "0 B");
      put(1023L, "1023 B");
      put(1024L, "1 KB");
      put(12_345L, "12.1 KB");
      put(10_123_456L, "9.7 MB");
      put(10_123_456_798L, "9.4 GB");;
    }};
    DATA_MAP.forEach((in, expected) -> {
      String humanReadableSize = FileUploadUtils.fileSizeFormatter(in);
      assertThat(humanReadableSize).isEqualTo(expected);
    });
  }

  @Test
  void getFileResourceResponseEntity() throws IOException {
    var uploadedFile = mock(OldUploadedFile.class);
    var fileContent = new byte[]{1, 2, 3, 4, 5};

    when(uploadedFile.getFileSizeBytes()).thenReturn(5L);
    when(uploadedFile.getFilename()).thenReturn("file");

    var responseEntity = FileUploadUtils.getFileResourceResponseEntity(
        uploadedFile,
        new InputStreamResource(new ByteArrayInputStream(fileContent))
    );

    //https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition
    var contentDisposition = "attachment; filename=\"file\"";

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getHeaders().toSingleValueMap())
        .containsEntry(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .containsEntry(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
        .containsEntry(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    assertThat(responseEntity.getBody().getInputStream().readAllBytes()).isEqualTo(fileContent);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 5})
  void getUploadIdList(int size) {
    var form = mock(FileUploadForm.class);
    var forms = IntStream.range(0, size).mapToObj(i -> form).toList();

    if (size > 0) {
      when(form.getUploadedFileId()).thenReturn(UUID.randomUUID());
    }

    var list = FileUploadUtils.getUploadIdList(forms);
    assertThat(list).hasSize(size);
  }
}