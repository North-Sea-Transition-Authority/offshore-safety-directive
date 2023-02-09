package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationFileDownloadController;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@ExtendWith(MockitoExtension.class)
class CaseEventFileServiceTest {

  @Mock
  CaseEventFileRepository caseEventFileRepository;

  @Mock
  FileUploadService fileUploadService;

  @InjectMocks
  private CaseEventFileService caseEventFileService;

  @Test
  void finalizeFileUpload_whenFilesNotFound_thenError() {

    var caseEventUuid = UUID.randomUUID();
    var caseEvent = new CaseEvent();
    caseEvent.setUuid(caseEventUuid);

    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(fileUuid);

    when(fileUploadService.getUploadedFiles(List.of(uploadedFileId)))
        .thenReturn(List.of());

    assertThatThrownBy(() -> caseEventFileService.finalizeFileUpload(caseEvent, List.of(fileUploadForm)))
        .hasMessage(
            "Unable to find all uploaded files. Expected IDs [%s] got [] for CaseEvent [%s]".formatted(
                fileUuid,
                caseEventUuid
            ));
  }

  @Test
  void finalizeFileUpload_verifyFinalized() {

    var nominationDetailVersion = 2;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationDetailVersion)
        .build();

    var fileUuid = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileUuid);

    var caseEvent = new CaseEvent();
    caseEvent.setNomination(nominationDetail.getNomination());
    caseEvent.setNominationVersion(nominationDetailVersion);

    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(fileUuid);

    var uploadedFile = new UploadedFile();

    when(fileUploadService.getUploadedFiles(List.of(uploadedFileId)))
        .thenReturn(List.of(uploadedFile));

    caseEventFileService.finalizeFileUpload(caseEvent, List.of(fileUploadForm));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<CaseEventFile>> caseEventFileCaptor = ArgumentCaptor.forClass(List.class);
    verify(caseEventFileRepository).saveAll(caseEventFileCaptor.capture());

    var caseEventFiles = caseEventFileCaptor.getValue();
    assertThat(caseEventFiles)
        .extracting(
            CaseEventFile::getCaseEvent,
            CaseEventFile::getUploadedFile
        )
        .containsExactly(
            Tuple.tuple(caseEvent, uploadedFile)
        );

  }

  @Test
  void getFileViewMapFromCaseEvents_whenTwoLinkedFiles_thenAssertResult() {
    var firstFile = UploadedFileTestUtil.builder()
        .withFilename("File A")
        .build();
    var secondFile = UploadedFileTestUtil.builder()
        .withFilename("File B")
        .build();
    var firstUploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(firstFile);
    var secondUploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(secondFile);
    var caseEvent = CaseEventTestUtil.builder().build();
    var firstCaseEventFileByFileName = new CaseEventFile();
    firstCaseEventFileByFileName.setCaseEvent(caseEvent);
    firstCaseEventFileByFileName.setUploadedFile(firstFile);

    var secondCaseEventFileByFileName = new CaseEventFile();
    secondCaseEventFileByFileName.setCaseEvent(caseEvent);
    secondCaseEventFileByFileName.setUploadedFile(secondFile);

    when(caseEventFileRepository.findAllByCaseEventIn(List.of(caseEvent)))
        .thenReturn(List.of(secondCaseEventFileByFileName, firstCaseEventFileByFileName));

    when(fileUploadService.getUploadedFileViewList(List.of(
        new UploadedFileId(secondFile.getId()),
        new UploadedFileId(firstFile.getId())
    )))
        .thenReturn(List.of(firstUploadedFileView, secondUploadedFileView));

    var result = caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent));

    var expectedFirstFileView = new CaseEventFileView(
        firstUploadedFileView,
        ReverseRouter.route(on(NominationFileDownloadController.class)
            .download(
                new NominationId(caseEvent.getNomination().getId()),
                new UploadedFileId(UUID.fromString(firstUploadedFileView.fileId()))
            ))
    );

    var expectedSecondFileView = new CaseEventFileView(
        secondUploadedFileView,
        ReverseRouter.route(on(NominationFileDownloadController.class)
            .download(
                new NominationId(caseEvent.getNomination().getId()),
                new UploadedFileId(UUID.fromString(secondUploadedFileView.fileId()))
            ))
    );

    assertThat(result)
        .containsOnlyKeys(caseEvent)
        .extractingByKey(caseEvent)
        .asList()
        .containsExactly(
            expectedFirstFileView,
            expectedSecondFileView
        );
  }

  @Test
  void getFileViewMapFromCaseEvents_whenNoLinkedFiles_thenAssertResult() {
    var caseEvent = CaseEventTestUtil.builder().build();

    when(caseEventFileRepository.findAllByCaseEventIn(List.of(caseEvent)))
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