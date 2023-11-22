package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUsage;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.OldUploadedFile;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class CaseEventFileServiceTest {

  private static ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private FileAssociationService fileAssociationService;

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private FileService fileService;

  @InjectMocks
  private CaseEventFileService caseEventFileService;

  @Test
  void finalizeFileUpload_whenFilesNotFound_thenError() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var caseEventUuid = UUID.randomUUID();
    var caseEvent = new CaseEvent();
    caseEvent.setUuid(caseEventUuid);

    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(fileUuid);

    when(fileUploadService.getUploadedFiles(List.of(uploadedFileId)))
        .thenReturn(List.of());

    assertThatThrownBy(
        () -> caseEventFileService.finalizeFileUpload(nominationDetail, caseEvent, List.of(fileUploadForm)))
        .hasMessage(
            "Unable to find all uploaded files. Expected IDs [%s] got [] for CaseEvent [%s]".formatted(
                fileUuid,
                caseEventUuid
            ));
  }

  @Test
  void finalizeFileUpload_verifyFileReferencesUpdated() {

    var nominationDetailVersion = 2;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationDetailVersion)
        .build();

    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);

    var caseEvent = CaseEventTestUtil.builder().build();
    caseEvent.setNomination(nominationDetail.getNomination());
    caseEvent.setNominationVersion(nominationDetailVersion);

    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(fileUuid);

    var uploadedFile = new OldUploadedFile();
    var fileAssociation = FileAssociationTestUtil.builder()
        .withUploadedFile(uploadedFile)
        .build();

    when(fileUploadService.getUploadedFiles(List.of(uploadedFileId)))
        .thenReturn(List.of(uploadedFile));

    var nominationDetailFileReferenceCaptor = ArgumentCaptor.forClass(NominationDetailFileReference.class);
    when(fileAssociationService.getAllByFileReferenceAndUploadedFileIds(
        nominationDetailFileReferenceCaptor.capture(),
        eq(List.of(uploadedFileId))
    )).thenReturn(List.of(fileAssociation));

    caseEventFileService.finalizeFileUpload(nominationDetail, caseEvent, List.of(fileUploadForm));

    var caseEventFileReferenceCaptor = ArgumentCaptor.forClass(CaseEventFileReference.class);
    verify(fileAssociationService).updateFileReferences(
        eq(List.of(fileAssociation)),
        caseEventFileReferenceCaptor.capture()
    );

    assertThat(nominationDetailFileReferenceCaptor.getValue())
        .extracting(NominationDetailFileReference::getReferenceId)
        .isEqualTo(new NominationDetailFileReference(nominationDetail).getReferenceId());

    assertThat(caseEventFileReferenceCaptor.getValue())
        .extracting(CaseEventFileReference::getReferenceId)
        .isEqualTo(new CaseEventFileReference(caseEvent).getReferenceId());
  }

  @Test
  void getFileViewMapFromCaseEvents_whenMultipleLinkedFiles_thenAssertResult() {
    var caseEvent = CaseEventTestUtil.builder().build();
    var firstFile = UploadedFileTestUtil.newBuilder()
        .withName("File a")
        .withUsageId(caseEvent.getUuid().toString())
        .build();
    var secondFile = UploadedFileTestUtil.newBuilder()
        .withName("File B") // Ensure sort is not case-sensitive
        .withUsageId(caseEvent.getUuid().toString())
        .build();
    var thirdFile = UploadedFileTestUtil.newBuilder()
        .withName("File c")
        .withUsageId(caseEvent.getUuid().toString())
        .build();

    when(fileService.findAllByUsageIdsWithUsageType(
        List.of(caseEvent.getUuid().toString()),
        FileUsageType.CASE_EVENT.getUsageType()
    ))
        .thenReturn(List.of(firstFile, thirdFile, secondFile));

    var result = caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent));

    assertThat(result)
        .containsOnlyKeys(caseEvent)
        .extractingByKey(caseEvent)
        .asInstanceOf(InstanceOfAssertFactories.list(FileSummaryView.class))
        .extracting(
            fileSummaryView -> fileSummaryView.uploadedFileView().fileName()
        )
        .containsExactly(
            firstFile.getName(),
            secondFile.getName(),
            thirdFile.getName()
        );
  }

  @Test
  void getFileViewMapFromCaseEvents_whenNoLinkedFiles_thenAssertResult() {
    var caseEvent = CaseEventTestUtil.builder().build();

    when(fileService.findAllByUsageIdsWithUsageType(
        List.of(caseEvent.getUuid().toString()),
        FileUsageType.CASE_EVENT.getUsageType()
    ))
        .thenReturn(List.of());

    var result = caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent));

    assertThat(result).isEmpty();
  }

  @Test
  void linkFilesToCaseEvent_whenValidFile_thenVerifyFileIsLinked() {

    var caseEvent = CaseEventTestUtil.builder().build();

    var formDescription = "description";
    var formFileId = UUID.randomUUID();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileDescription(formDescription);
    uploadedFileForm.setFileId(formFileId);

    var file = UploadedFileTestUtil.newBuilder()
        .withId(formFileId)
        .withUploadedBy(USER.wuaId().toString())
        .build();

    when(fileService.findAll(Set.of(formFileId)))
        .thenReturn(List.of(file));

    caseEventFileService.linkFilesToCaseEvent(
        caseEvent,
        List.of(uploadedFileForm),
        FileDocumentType.CASE_NOTE
    );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Function<FileUsage.Builder, FileUsage>> fileUsageCaptor = ArgumentCaptor.forClass(Function.class);
    verify(fileService).updateUsageAndDescription(eq(file), fileUsageCaptor.capture(), eq(formDescription));

    fileUsageCaptor.getAllValues()
        .forEach(builderFileUsageFunction -> {
          var builder = FileUsage.newBuilder();
          assertThat(builderFileUsageFunction.apply(builder))
              .extracting(
                  FileUsage::usageId,
                  FileUsage::usageType,
                  FileUsage::documentType
              )
              .containsExactly(
                  caseEvent.getUuid().toString(),
                  FileUsageType.CASE_EVENT.getUsageType(),
                  FileDocumentType.CASE_NOTE.getDocumentType()
              );
        });
  }

}