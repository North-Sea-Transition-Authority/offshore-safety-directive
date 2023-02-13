package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.NominationDetailFileService;

@ExtendWith(MockitoExtension.class)
class GeneralCaseNoteSubmissionServiceTest {

  @Mock
  private CaseEventService caseEventService;

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private NominationDetailFileService nominationDetailFileService;

  @InjectMocks
  private GeneralCaseNoteSubmissionService generalCaseNoteSubmissionService;

  @Test
  void submitCaseNote_verifyCalls() {
    var subject = "Test subject";
    var caseNoteText = "Test text";
    var detail = NominationDetailTestUtil.builder().build();
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(UUID.randomUUID());
    var form = new GeneralCaseNoteForm();
    form.getCaseNoteSubject().setInputValue(subject);
    form.getCaseNoteText().setInputValue(caseNoteText);
    form.setCaseNoteFiles(List.of(fileUploadForm));


    generalCaseNoteSubmissionService.submitCaseNote(detail, form);

    verify(caseEventService).createGeneralCaseNoteEvent(detail, subject, caseNoteText, List.of(fileUploadForm));
    verify(fileUploadService).updateFileUploadDescriptions(List.of(fileUploadForm));
    verify(nominationDetailFileService).submitAndCleanFiles(detail, List.of(fileUploadForm), VirtualFolder.CASE_NOTES);

    verifyNoMoreInteractions(caseEventService);
    verifyNoMoreInteractions(fileUploadService);
    verifyNoMoreInteractions(nominationDetailFileService);

  }
}