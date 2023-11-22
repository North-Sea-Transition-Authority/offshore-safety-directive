package uk.co.nstauthority.offshoresafetydirective.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.co.nstauthority.offshoresafetydirective.file.virus.VirusCheckFailedException;
import uk.co.nstauthority.offshoresafetydirective.file.virus.VirusCheckService;

@ExtendWith(MockitoExtension.class)
class FileUploadValidationServiceTest {

  @Mock
  VirusCheckService virusCheckService;

  static FileUploadConfig fileUploadConfig;

  private static FileUploadValidationService fileUploadValidationService;
  private static String allowedFilenameAndExtension;
  private static long allowedFileSize;
  private static MultipartFile multipartFile;

  @BeforeAll
  static void beforeAll() throws IOException {
    fileUploadConfig = new FileUploadConfig(1024, ".txt, .jpg, .jpeg");

    multipartFile = Mockito.mock(MultipartFile.class);
    when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));
    allowedFilenameAndExtension = "text.txt";
    allowedFileSize = 123L;
  }

  @BeforeEach
  void setUp() {
    fileUploadValidationService = new FileUploadValidationService(virusCheckService,
        fileUploadConfig);
  }

  @Test
  void validateFileUpload_CheckFileExtensionAllowed_NotAllowed() {

    long fileSize = allowedFileSize;
    String filename = "text.exe";

    var uploadErrorType = fileUploadValidationService.validateFileUpload(multipartFile, fileSize, filename,
        fileUploadConfig.getDefaultPermittedFileExtensions());

    assertThat(uploadErrorType).contains(UploadErrorType.EXTENSION_NOT_ALLOWED);
  }

  @Test
  void validateFileUpload_CheckFileExtensionAllowed_IsAllowed() {

    long fileSize = allowedFileSize;
    var filename = allowedFilenameAndExtension;

    var uploadErrorType = fileUploadValidationService.validateFileUpload(multipartFile, fileSize, filename,
        fileUploadConfig.getDefaultPermittedFileExtensions());

    assertThat(uploadErrorType).isEmpty();
  }

  @Test
  void validateFileUpload_CheckFileSizeAllowed_NotAllowed() {

    long fileSize = 1025L;
    var filename = allowedFilenameAndExtension;

    var uploadErrorType = fileUploadValidationService.validateFileUpload(multipartFile, fileSize, filename,
        fileUploadConfig.getDefaultPermittedFileExtensions());

    assertThat(uploadErrorType).contains(UploadErrorType.MAX_FILE_SIZE_EXCEEDED);
  }

  @Test
  void validateFileUpload_CheckFileSizeAllowed_IsAllowed() {

    long fileSize = allowedFileSize;
    var filename = allowedFilenameAndExtension;

    var uploadErrorType = fileUploadValidationService.validateFileUpload(multipartFile, fileSize, filename,
        fileUploadConfig.getDefaultPermittedFileExtensions());

    assertThat(uploadErrorType).isEmpty();
  }

  @Test
  void validateFileUpload_CheckFileHasVirus_VirusFound() throws IOException {
    long fileSize = allowedFileSize;
    var filename = allowedFilenameAndExtension;
    when(virusCheckService.hasVirus(any(InputStream.class))).thenReturn(true);

    var uploadErrorType = fileUploadValidationService.validateFileUpload(multipartFile, fileSize, filename,
        fileUploadConfig.getDefaultPermittedFileExtensions());

    assertThat(uploadErrorType).contains(UploadErrorType.VIRUS_FOUND_IN_FILE);
  }

  @Test
  void validateFileUpload_CheckFileHasVirus_VirusNotFound() throws IOException {

    long fileSize = allowedFileSize;
    var filename = allowedFilenameAndExtension;
    when(virusCheckService.hasVirus(any(InputStream.class))).thenReturn(false);

    var uploadErrorType = fileUploadValidationService.validateFileUpload(multipartFile, fileSize, filename,
        fileUploadConfig.getDefaultPermittedFileExtensions());

    assertThat(uploadErrorType).isEmpty();
  }

  @Test
  void validateFileUpload_VirusCheckFailed() throws IOException {

    long fileSize = allowedFileSize;
    var filename = allowedFilenameAndExtension;
    var allowedFileExtensions = fileUploadConfig.getDefaultPermittedFileExtensions();

    when(virusCheckService.hasVirus(any(InputStream.class))).thenThrow(MockitoException.class);

    assertThatExceptionOfType(VirusCheckFailedException.class).isThrownBy(() -> {
      var uploadErrorType = fileUploadValidationService.validateFileUpload(multipartFile, fileSize, filename,
          allowedFileExtensions);
    });
  }
}