package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import uk.co.nstauthority.offshoresafetydirective.file.FileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadUtils;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadValidationService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadErrorType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationFileServiceTest {

  private static final String FILENAME = FileTestUtil.VALID_FILENAME;
  private static final long FILE_SIZE = FileTestUtil.VALID_FILE_SIZE;
  private static final String CONTENT_TYPE = FileTestUtil.VALID_CONTENT_TYPE;
  private static final VirtualFolder VIRTUAL_fOLDER = FileTestUtil.VALID_VIRTUAL_FOLDER;
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".txt");

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private FileUploadValidationService fileUploadValidationService;

  @Mock
  private NominationDetailFileService nominationDetailFileService;

  @InjectMocks
  private NominationFileService nominationFileService;

  @ParameterizedTest
  @EnumSource(UploadErrorType.class)
  void processFileUpload_failsValidation_returnsInvalidResult(UploadErrorType uploadErrorType) {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var multipartFile = FileTestUtil.multipartFileMockBuilder()
        .withMockedName(FILENAME)
        .withMockedSize(FILE_SIZE)
        .build();

    when(fileUploadService.sanitiseFilename(FILENAME)).thenReturn(FILENAME);
    when(fileUploadValidationService.validateFileUpload(multipartFile, FILE_SIZE, FILENAME, ALLOWED_EXTENSIONS))
        .thenReturn(Optional.of(uploadErrorType));

    var fileUploadResult = nominationFileService.processFileUpload(nominationDetail,
        VirtualFolder.NOMINATION_DECISION, multipartFile, ALLOWED_EXTENSIONS);

    assertThat(fileUploadResult.isValid()).isFalse();
  }

  @Test
  void processFileUpload_passesValidationAndUploadsFile_verifyInteractions() {

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var multipartFile = FileTestUtil.multipartFileMockBuilder()
        .withMockedName(FILENAME)
        .withMockedSize(FILE_SIZE)
        .withMockedContentType(CONTENT_TYPE)
        .build();

    var uploadedFile = UploadedFileTestUtil.builder().build();

    when(fileUploadService.sanitiseFilename(FILENAME)).thenReturn(FILENAME);
    when(fileUploadService.createUploadedFile(VIRTUAL_fOLDER, FILE_SIZE, FILENAME, CONTENT_TYPE))
        .thenReturn(uploadedFile);

    var fileUploadResult = nominationFileService.processFileUpload(nominationDetail,
        VirtualFolder.NOMINATION_DECISION, multipartFile, ALLOWED_EXTENSIONS);

    assertThat(fileUploadResult.isValid()).isTrue();
  }

  @Test
  void deleteFile_fileDeleted_supportingDocAndFileDeleted() {
    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .build();
    var nominationDetailFile = NominationDetailFileTestUtil.builder().build();

    when(fileUploadService.findUploadedFile(uploadedFileId)).thenReturn(Optional.of(uploadedFile));
    when(nominationDetailFileService.getNominationDetailFileForNomination(nominationDetail.getNomination(),
        uploadedFile))
        .thenReturn(List.of(nominationDetailFile));

    var fileDeleteResult = nominationFileService.deleteFile(nominationDetail,
        new UploadedFileId(fileUuid));

    verify(nominationDetailFileService).deleteNominationDetailFile(nominationDetail, uploadedFile);
    verify(fileUploadService, times(1)).deleteFile(uploadedFile);
    assertThat(fileDeleteResult.isValid()).isTrue();
  }

  @Test
  void deleteFile_fileDeletedButExistsInOtherVersion_onlySupportingDocDeleted() {
    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var firstNominationDetail = NominationDetailTestUtil.builder()
        .withId(1)
        .build();
    var secondNominationDetail = NominationDetailTestUtil.builder()
        .withId(2)
        .build();

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .build();

    var nominationDetailFileA = NominationDetailFileTestUtil.builder()
        .withNominationDetail(firstNominationDetail)
        .build();
    var nominationDetailFileB = NominationDetailFileTestUtil.builder()
        .withNominationDetail(secondNominationDetail)
        .build();

    when(fileUploadService.findUploadedFile(uploadedFileId)).thenReturn(Optional.of(uploadedFile));

    when(nominationDetailFileService.getNominationDetailFileForNomination(firstNominationDetail.getNomination(),
        uploadedFile))
        .thenReturn(List.of(nominationDetailFileA, nominationDetailFileB));

    var fileDeleteResult = nominationFileService.deleteFile(firstNominationDetail,
        new UploadedFileId(fileUuid));

    verify(nominationDetailFileService, times(1)).deleteNominationDetailFile(firstNominationDetail, uploadedFile);
    verify(fileUploadService, never()).deleteFile(uploadedFile);
    assertThat(fileDeleteResult.isValid()).isTrue();
  }

  @Test
  void handleDownload_verifyCalls() {
    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .build();

    var inputStream = InputStream.nullInputStream();

    var nominationDetailFile = NominationDetailFileTestUtil.builder().build();

    when(fileUploadService.findUploadedFile(uploadedFileId))
        .thenReturn(Optional.of(uploadedFile));

    when(nominationDetailFileService.getNominationDetailFileForNominationDetail(nominationDetail, uploadedFile))
        .thenReturn(Optional.of(nominationDetailFile));

    when(fileUploadService.downloadFile(uploadedFile))
        .thenReturn(inputStream);

    var result = nominationFileService.handleDownload(nominationDetail, new UploadedFileId(fileUuid));

    assertThat(result).isEqualTo(
        FileUploadUtils.getFileResourceResponseEntity(uploadedFile, new InputStreamResource(inputStream)));
  }
}