package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationDto;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationType;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.OldUploadedFile;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@ExtendWith(MockitoExtension.class)
class CaseEventFileServiceTest {

  @Mock
  FileAssociationService fileAssociationService;

  @Mock
  FileUploadService fileUploadService;

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
    var firstFile = UploadedFileTestUtil.builder()
        .withFilename("File a")
        .build();
    var secondFile = UploadedFileTestUtil.builder()
        .withFilename("File B") // Ensure sort is not case-sensitive
        .build();
    var thirdFile = UploadedFileTestUtil.builder()
        .withFilename("File c")
        .build();
    var firstUploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(firstFile);
    var secondUploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(secondFile);
    var thirdUploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(thirdFile);
    var caseEvent = CaseEventTestUtil.builder().build();

    var firstFileAssociationDtoByFileName = new FileAssociationDto(
        new UploadedFileId(firstFile.getId()),
        caseEvent.getUuid().toString()
    );
    var secondFileAssociationDtoByFileName = new FileAssociationDto(
        new UploadedFileId(secondFile.getId()),
        caseEvent.getUuid().toString()
    );
    var thirdFileAssociationDtoByFileName = new FileAssociationDto(
        new UploadedFileId(thirdFile.getId()),
        caseEvent.getUuid().toString()
    );

    when(fileAssociationService.getUploadedFileAssociationDtosByReferenceTypeAndReferenceIds(
        FileAssociationType.CASE_EVENT,
        Set.of(caseEvent.getUuid().toString())
    ))
        .thenReturn(
            List.of(secondFileAssociationDtoByFileName, thirdFileAssociationDtoByFileName,
                firstFileAssociationDtoByFileName)
        );

    when(fileUploadService.getUploadedFileViewList(List.of(
        new UploadedFileId(secondFile.getId()),
        new UploadedFileId(thirdFile.getId()),
        new UploadedFileId(firstFile.getId())
    )))
        .thenReturn(List.of(secondUploadedFileView, thirdUploadedFileView, firstUploadedFileView));

    var result = caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent));

    var expectedFirstFileView = new FileSummaryView(
        firstUploadedFileView,
        ReverseRouter.route(on(CaseEventFileDownloadController.class)
            .download(
                new NominationId(caseEvent.getNomination().getId()),
                new CaseEventId(caseEvent.getUuid()),
                new UploadedFileId(UUID.fromString(firstUploadedFileView.fileId()))
            ))
    );
    var expectedSecondFileView = new FileSummaryView(
        secondUploadedFileView,
        ReverseRouter.route(on(CaseEventFileDownloadController.class)
            .download(
                new NominationId(caseEvent.getNomination().getId()),
                new CaseEventId(caseEvent.getUuid()),
                new UploadedFileId(UUID.fromString(secondUploadedFileView.fileId()))
            ))
    );
    var expectedThirdFileView = new FileSummaryView(
        thirdUploadedFileView,
        ReverseRouter.route(on(CaseEventFileDownloadController.class)
            .download(
                new NominationId(caseEvent.getNomination().getId()),
                new CaseEventId(caseEvent.getUuid()),
                new UploadedFileId(UUID.fromString(thirdUploadedFileView.fileId()))
            ))
    );

    assertThat(result)
        .containsOnlyKeys(caseEvent)
        .extractingByKey(caseEvent)
        .asList()
        .containsExactly(
            expectedFirstFileView,
            expectedSecondFileView,
            expectedThirdFileView
        );
  }

  @Test
  void getFileViewMapFromCaseEvents_whenNoLinkedFiles_thenAssertResult() {
    var caseEvent = CaseEventTestUtil.builder().build();

    when(fileAssociationService.getUploadedFileAssociationDtosByReferenceTypeAndReferenceIds(
        FileAssociationType.CASE_EVENT,
        Set.of(caseEvent.getUuid().toString())
    ))
        .thenReturn(List.of());

    when(fileUploadService.getUploadedFileViewList(List.of()))
        .thenReturn(List.of());

    var result = caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent));

    assertThat(result)
        .containsExactly(
            entry(caseEvent, List.of())
        );
  }

}