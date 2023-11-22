package uk.co.nstauthority.offshoresafetydirective.file;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public class FileTestUtil {

  public static final long VALID_FILE_SIZE = 123L;
  public static final String VALID_FILENAME = "file.txt";
  public static final String VALID_CONTENT_TYPE = "Text/plain";
  public static final String VALID_S3_KEY = String.valueOf(UUID.randomUUID());
  public static final VirtualFolder VALID_VIRTUAL_FOLDER = VirtualFolder.NOMINATION_DECISION;
  public static final String VALID_BUCKET_NAME = "TEST-BUCKET";

  private FileTestUtil() {
    throw new AssertionError();
  }

  public static OldUploadedFile createValidUploadedFile() {
    return createValidUploadedFile(VALID_FILENAME, VALID_FILE_SIZE, VALID_CONTENT_TYPE);
  }

  public static OldUploadedFile createValidUploadedFile(String filename, long fileSize, String contentType) {
    var uuid = UUID.randomUUID();
    var uploadedFile = new OldUploadedFile();
    uploadedFile.setId(uuid);
    uploadedFile.setFileKey(VALID_S3_KEY);
    uploadedFile.setBucketName(VALID_BUCKET_NAME);
    uploadedFile.setVirtualFolder(VALID_VIRTUAL_FOLDER);
    uploadedFile.setFilename(filename);
    uploadedFile.setFileSizeBytes(fileSize);
    uploadedFile.setFileContentType(contentType);

    return uploadedFile;
  }

  public static MultipartFileMockBuilder multipartFileMockBuilder() {
    return new MultipartFileMockBuilder();
  }

  public static class MultipartFileMockBuilder {

    private String filename = VALID_FILENAME;
    private Long fileSize  = VALID_FILE_SIZE;
    private String contentType = VALID_CONTENT_TYPE;
    private MultipartFile multipartFile;

    private MultipartFileMockBuilder() {
      this.multipartFile = mock(MultipartFile.class);
    }

    public MultipartFileMockBuilder withMockedSize(Long size) {
      this.fileSize = size;
      when(multipartFile.getSize()).thenReturn(fileSize);
      return this;
    }

    public MultipartFileMockBuilder withMockedName(String name) {
      this.filename = name;
      when(multipartFile.getOriginalFilename()).thenReturn(filename);
      return this;
    }

    public MultipartFileMockBuilder withMockedContentType(String contentType) {
      this.contentType = contentType;
      when(multipartFile.getContentType()).thenReturn(contentType);
      return this;
    }

    public MultipartFileMockBuilder withMockedInputStream() throws IOException {
      when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));
      return this;
    }

    public MultipartFile build() {
      return multipartFile;

    }
  }

  public static MultipartFile createMultipartFileMock(String filename, Long fileSize, String contentType) throws IOException {
    var multipartFile = mock(MultipartFile.class);

    when(multipartFile.getSize()).thenReturn(fileSize);
    when(multipartFile.getContentType()).thenReturn(contentType);
    when(multipartFile.getOriginalFilename()).thenReturn(filename);
    when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));

    return multipartFile;
  }

  public static FileUploadConfig validFileUploadConfig() {
    return new FileUploadConfig(1024, ".txt, .jpg, .jpeg");
  }
}
