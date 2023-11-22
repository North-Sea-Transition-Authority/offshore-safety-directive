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
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@ExtendWith(MockitoExtension.class)
class GeneralCaseNoteSubmissionServiceTest {

  @Mock
  private CaseEventService caseEventService;

  @InjectMocks
  private GeneralCaseNoteSubmissionService generalCaseNoteSubmissionService;

  @Test
  void submitCaseNote_verifyCalls() {
    var subject = "Test subject";
    var caseNoteText = "Test text";
    var detail = NominationDetailTestUtil.builder().build();
    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setUploadedFileId(UUID.randomUUID());
    var form = new GeneralCaseNoteForm();
    form.getCaseNoteSubject().setInputValue(subject);
    form.getCaseNoteText().setInputValue(caseNoteText);
    form.setCaseNoteFiles(List.of(uploadedFileForm));


    generalCaseNoteSubmissionService.submitCaseNote(detail, form);

    verify(caseEventService).createGeneralCaseNoteEvent(detail, subject, caseNoteText, List.of(uploadedFileForm));

    verifyNoMoreInteractions(caseEventService);

  }
}