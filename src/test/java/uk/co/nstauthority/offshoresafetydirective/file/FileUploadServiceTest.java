package uk.co.nstauthority.offshoresafetydirective.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

  private static final UploadedFile UPLOADED_FILE = FileTestUtil.createValidUploadedFile();
  private static final FileUploadConfig FILE_UPLOAD_CONFIG = FileTestUtil.validFileUploadConfig();

  @Mock
  private FileUploadStorageService fileUploadStorageService;

  @Mock
  private UploadedFilePersistenceService uploadedFilePersistenceService;

  @Mock
  private UploadedFileRepository uploadedFileRepository;

  private FileUploadService fileUploadService;


  @BeforeEach
  void setUp() {
    fileUploadService = new FileUploadService(
        fileUploadStorageService,
        uploadedFilePersistenceService,
        FILE_UPLOAD_CONFIG
    );
  }

  @Test
  void uploadFile() {
    var multipartFile = mock(MultipartFile.class);
    fileUploadService.uploadFile(multipartFile, UPLOADED_FILE);
    verify(fileUploadStorageService).uploadFile(multipartFile, UPLOADED_FILE);
  }

  @Test
  void createUploadedFile() {
    fileUploadService.createUploadedFile(
        FileTestUtil.VALID_VIRTUAL_FOLDER,
        FileTestUtil.VALID_FILE_SIZE,
        FileTestUtil.VALID_FILENAME,
        FileTestUtil.VALID_CONTENT_TYPE
    );

    verify(uploadedFilePersistenceService).createUploadedFile(
        FileTestUtil.VALID_VIRTUAL_FOLDER,
        FileTestUtil.VALID_FILE_SIZE,
        FileTestUtil.VALID_FILENAME,
        FileTestUtil.VALID_CONTENT_TYPE
    );
  }

  @Test
  void deleteFile_VerifyInteractions() {
    fileUploadService.deleteFile(UPLOADED_FILE);

    verify(fileUploadStorageService).deleteFile(UPLOADED_FILE);
    verify(uploadedFilePersistenceService).deleteFile(UPLOADED_FILE);
  }

  @Test
  void downloadFile_VerifyInteractions() {
    fileUploadService.downloadFile(UPLOADED_FILE);
    verify(fileUploadStorageService).downloadFile(UPLOADED_FILE);
  }

  @Test
  void updateFileUploadDescriptions() {
    var uuid = UUID.randomUUID();
    var fileUploadForm = mock(FileUploadForm.class);

    when(fileUploadForm.getUploadedFileId()).thenReturn(uuid);
    when(fileUploadService.findUploadedFile(new UploadedFileId(uuid))).thenReturn(Optional.of(UPLOADED_FILE));

    fileUploadService.updateFileUploadDescriptions(List.of(fileUploadForm));

    verify(uploadedFilePersistenceService).updateFileDescription(UPLOADED_FILE,
        fileUploadForm.getUploadedFileDescription());
  }

  @Test
  void buildFileUploadTemplate() {
    var downloadUrl = "download";
    var uploadUrl = "upload";
    var deleteUrl = "delete";

    var fileUploadTemplate =
        fileUploadService.buildFileUploadTemplate(downloadUrl, uploadUrl, deleteUrl);

    assertThat(fileUploadTemplate)
        .extracting(
            FileUploadTemplate::downloadUrl,
            FileUploadTemplate::uploadUrl,
            FileUploadTemplate::deleteUrl,
            FileUploadTemplate::maxAllowedSize,
            FileUploadTemplate::allowedExtensions
        )
        .contains(
            downloadUrl,
            uploadUrl,
            deleteUrl,
            FILE_UPLOAD_CONFIG.getMaxFileUploadBytes().toString(),
            String.join(",", FILE_UPLOAD_CONFIG.getAllowedFileExtensions())
        );
  }

  @Test
  void sanitiseFilename() {
    var filename = "this%is%test.txt";

    var sanitiseFilename = fileUploadService.sanitiseFilename(filename);

    assertThat(sanitiseFilename).isEqualTo("this_is_test.txt");
  }

  @Test
  void getUploadedFileViewList() {
    var uuid = UUID.randomUUID();
    var fileUploadIdList = List.of(new UploadedFileId(uuid));

    when(uploadedFilePersistenceService.getUploadedFilesByIdList(fileUploadIdList))
        .thenReturn(List.of(UPLOADED_FILE));

    var uploadedFileViews = fileUploadService.getUploadedFileViewList(fileUploadIdList);

    assertThat(uploadedFileViews)
        .extracting(
            UploadedFileView::getFileId,
            UploadedFileView::getFileName,
            UploadedFileView::getFileSize,
            UploadedFileView::getFileDescription,
            UploadedFileView::getFileUploadedTime
        )
        .contains(
            tuple(
                UPLOADED_FILE.getId().toString(),
                UPLOADED_FILE.getFilename(),
                FileUploadUtils.fileSizeFormatter(UPLOADED_FILE.getFileSizeBytes()),
                UPLOADED_FILE.getDescription(),
                UPLOADED_FILE.getUploadedTimeStamp()
            )
        );
  }

  @Test
  void createFileUploadForm() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(UUID.randomUUID());
    uploadedFile.setDescription("description");
    uploadedFile.setUploadedTimeStamp(Instant.now());

    var form = fileUploadService.createFileUploadForm(uploadedFile);

    assertThat(form.getUploadedFileId()).isEqualTo(uploadedFile.getId());
    assertThat(form.getUploadedFileDescription()).isEqualTo(uploadedFile.getDescription());
  }

  @Test
  void getUploadedFiles() {
    var uuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(uuid);
    var uploadedFile = new UploadedFile();

    when(uploadedFilePersistenceService.getUploadedFilesByIdList(List.of(uploadedFileId)))
        .thenReturn(List.of(uploadedFile));

    var result = fileUploadService.getUploadedFiles(List.of(uploadedFileId));

    assertThat(result).containsExactly(uploadedFile);
  }

  @Test
  void getUploadedFileViewListFromForms() {
    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(fileUuid);

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(fileUuid);
    uploadedFile.setFileSizeBytes(1L);

    when(uploadedFilePersistenceService.getUploadedFilesByIdList(List.of(uploadedFileId)))
        .thenReturn(List.of(uploadedFile));

    var fileUploadServiceSpy = spy(fileUploadService);
    var result = fileUploadServiceSpy.getUploadedFileViewListFromForms(List.of(fileUploadForm));

    verify(fileUploadServiceSpy).getUploadedFileViewList(List.of(uploadedFileId));
    assertThat(result).isEqualTo(fileUploadService.getUploadedFileViewList(List.of(uploadedFileId)));
  }
}