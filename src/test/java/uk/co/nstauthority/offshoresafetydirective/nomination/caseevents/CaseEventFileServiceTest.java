package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

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

    var caseEventFileCaptor = ArgumentCaptor.forClass(List.class);
    verify(caseEventFileRepository).saveAll(caseEventFileCaptor.capture());

    @SuppressWarnings("unchecked")
    var caseEventFiles = (List<CaseEventFile>) caseEventFileCaptor.getValue();
    assertThat(caseEventFiles)
        .extracting(
            CaseEventFile::getCaseEvent,
            CaseEventFile::getUploadedFile
        )
        .containsExactly(
            Tuple.tuple(caseEvent, uploadedFile)
        );

  }

}