package uk.co.nstauthority.offshoresafetydirective.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
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
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class FileControllerHelperServiceTest {

  private static final String FILENAME = FileTestUtil.VALID_FILENAME;
  private static final long FILE_SIZE = FileTestUtil.VALID_FILE_SIZE;
  private static final String CONTENT_TYPE = FileTestUtil.VALID_CONTENT_TYPE;
  private static final VirtualFolder VIRTUAL_FOLDER = FileTestUtil.VALID_VIRTUAL_FOLDER;
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".txt");
  private static final String PURPOSE = "purpose";

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private FileUploadValidationService fileUploadValidationService;

  @Mock
  private FileAssociationService fileAssociationService;

  @InjectMocks
  private FileControllerHelperService fileControllerHelperService;

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

    var fileUploadResult = fileControllerHelperService.processFileUpload(
        new TestFileAssociationReference(nominationDetail),
        PURPOSE,
        VIRTUAL_FOLDER,
        multipartFile,
        ALLOWED_EXTENSIONS
    );

    assertFalse(fileUploadResult.isValid());
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
    when(fileUploadValidationService.validateFileUpload(multipartFile, FILE_SIZE, FILENAME, ALLOWED_EXTENSIONS))
        .thenReturn(Optional.empty());
    when(fileUploadService.createUploadedFile(VIRTUAL_FOLDER, FILE_SIZE, FILENAME, CONTENT_TYPE))
        .thenReturn(uploadedFile);

    var fileUploadResult = fileControllerHelperService.processFileUpload(
        new TestFileAssociationReference(nominationDetail),
        PURPOSE,
        VIRTUAL_FOLDER,
        multipartFile,
        ALLOWED_EXTENSIONS
    );

    assertTrue(fileUploadResult.isValid());
  }

  @Test
  void deleteFile_whenHasFileAssociation_verifyValid() {
    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var fileReference = new TestFileAssociationReference(nominationDetail);
    var fileAssociation = FileAssociationTestUtil.builder().build();

    when(fileAssociationService.findFileAssociation(fileReference, uploadedFileId))
        .thenReturn(Optional.of(fileAssociation));
    var fileDeleteResult = fileControllerHelperService.deleteFile(fileReference, uploadedFileId);

    verify(fileAssociationService).deleteFileAssociation(fileAssociation);
    assertTrue(fileDeleteResult.isValid());
  }

  @Test
  void deleteFile_whenNoFileAssociation_verifyInvalid() {
    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var fileReference = new TestFileAssociationReference(nominationDetail);

    when(fileAssociationService.findFileAssociation(fileReference, uploadedFileId))
        .thenReturn(Optional.empty());
    var fileDeleteResult = fileControllerHelperService.deleteFile(fileReference, uploadedFileId);

    verify(fileAssociationService, never()).deleteFileAssociation(any());
    assertFalse(fileDeleteResult.isValid());
  }

  @Test
  void downloadFile_validDownload() {
    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .build();

    var inputStream = InputStream.nullInputStream();
    var detailFile = FileAssociationTestUtil.builder().build();
    var fileReference = new TestFileAssociationReference(nominationDetail);

    when(fileUploadService.findUploadedFile(uploadedFileId))
        .thenReturn(Optional.of(uploadedFile));

    when(fileAssociationService.findFileAssociation(fileReference, uploadedFileId))
        .thenReturn(Optional.of(detailFile));

    when(fileUploadService.downloadFile(uploadedFile))
        .thenReturn(inputStream);

    var result = fileControllerHelperService.downloadFile(fileReference, new UploadedFileId(fileUuid));

    assertThat(result).isEqualTo(
        FileUploadUtils.getFileResourceResponseEntity(uploadedFile, new InputStreamResource(inputStream)));
  }

  @Test
  void downloadFile_whenUploadedFileIdDoesNotExist_thenVerifyError() {
    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var fileReference = new TestFileAssociationReference(nominationDetail);

    when(fileUploadService.findUploadedFile(uploadedFileId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fileControllerHelperService.downloadFile(fileReference, uploadedFileId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No uploaded file found with UUID [%s]".formatted(uploadedFileId.uuid()));
  }

  @Test
  void downloadFile_whenFileReferenceIsInvalid_thenVerifyError() {
    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .build();
    var fileReference = new TestFileAssociationReference(nominationDetail);

    when(fileUploadService.findUploadedFile(uploadedFileId))
        .thenReturn(Optional.of(uploadedFile));

    when(fileAssociationService.findFileAssociation(fileReference, uploadedFileId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fileControllerHelperService.downloadFile(fileReference, uploadedFileId))
        .isInstanceOf(OsdEntityNotFoundException.class)
        .hasMessage("No files with reference type [%s] and reference id [%s] and file id [%s]".formatted(
            fileReference.getFileReferenceType(),
            fileReference.getReferenceId(),
            uploadedFile.getId()
        ));
  }

  static class TestFileAssociationReference implements FileAssociationReference {

    private final NominationDetailId nominationDetailId;

    public TestFileAssociationReference(NominationDetail nominationDetail) {
      this.nominationDetailId = NominationDetailDto.fromNominationDetail(nominationDetail).nominationDetailId();
    }

    @Override
    public FileAssociationType getFileReferenceType() {
      return FileAssociationType.NOMINATION_DETAIL;
    }

    @Override
    public String getReferenceId() {
      return nominationDetailId.toString();
    }

  }
}