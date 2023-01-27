package uk.co.nstauthority.offshoresafetydirective.file;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.co.nstauthority.offshoresafetydirective.file.s3.S3ClientService;

@ExtendWith(MockitoExtension.class)
class FileUploadStorageServiceTest {

  @Mock
  private S3ClientService s3ClientService;

  @InjectMocks
  private FileUploadStorageService fileUploadStorageService;
  private static MultipartFile multipartFile;
  private static final UploadedFile uploadedFile = FileTestUtil.createValidUploadedFile();

  @BeforeAll
  static void beforeAll() throws IOException {
    multipartFile = FileTestUtil.createMultipartFileMock(FileTestUtil.VALID_FILENAME, FileTestUtil.VALID_FILE_SIZE,
        FileTestUtil.VALID_CONTENT_TYPE);
  }

  @Test
  void uploadFile_UploadFailed() {
    doThrow(MockitoException.class).when(s3ClientService)
        .storeFile(eq(FileTestUtil.VALID_BUCKET_NAME), anyString(), any(InputStream.class), any(ObjectMetadata.class));

    assertThatExceptionOfType(FileUploadException.class).isThrownBy(
        () -> fileUploadStorageService.uploadFile(multipartFile, uploadedFile));
  }

  @Test
  void uploadFile() {
    when(s3ClientService.getBucketName()).thenReturn(FileTestUtil.VALID_BUCKET_NAME);
    fileUploadStorageService.uploadFile(multipartFile, uploadedFile);
    verify(s3ClientService).storeFile(eq(FileTestUtil.VALID_BUCKET_NAME), anyString(), any(InputStream.class),
        any(ObjectMetadata.class));
  }

  @Test
  void deleteFile_FileDeleted() {
    fileUploadStorageService.deleteFile(uploadedFile);

    verify(s3ClientService).deleteFile(uploadedFile.getBucketName(), uploadedFile.getFileKey());
  }

  @Test
  void downloadFile_VerifyInteractions() {
    fileUploadStorageService.downloadFile(uploadedFile);
    verify(s3ClientService).fetchFile(uploadedFile.getBucketName(), uploadedFile.getFileKey());
  }
}