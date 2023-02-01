package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    var form = new GeneralCaseNoteForm();
    form.getCaseNoteSubject().setInputValue(subject);
    form.getCaseNoteText().setInputValue(caseNoteText);
    generalCaseNoteSubmissionService.submitCaseNote(detail, form);

    verify(caseEventService).createGeneralCaseNoteEvent(detail, subject, caseNoteText);
    verifyNoMoreInteractions(caseEventService);

  }
}