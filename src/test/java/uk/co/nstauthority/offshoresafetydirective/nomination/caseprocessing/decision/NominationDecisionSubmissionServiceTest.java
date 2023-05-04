package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;

@ExtendWith(MockitoExtension.class)
class NominationDecisionSubmissionServiceTest {

  @Mock
  private CaseEventService caseEventService;

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private FileAssociationService fileAssociationService;

  @Mock
  private NominationDetailService nominationDetailService;

  @InjectMocks
  private NominationDecisionSubmissionService nominationDecisionSubmissionService;

  @Test
  void submitNominationDecision_verifyCalls() {
    var date = LocalDate.now();
    var comment = "comment";
    var decision = NominationDecision.NO_OBJECTION;
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var form = new NominationDecisionForm();
    var fileUploadForm = new FileUploadForm();
    form.setNominationDecision(decision);
    form.setDecisionFiles(List.of(fileUploadForm));
    form.getDecisionDate().setDate(date);
    form.getComments().setInputValue(comment);

    nominationDecisionSubmissionService.submitNominationDecision(nominationDetail, form);

    verify(caseEventService).createDecisionEvent(nominationDetail, date, comment, decision, List.of(fileUploadForm));
    verify(fileUploadService).updateFileUploadDescriptions(List.of(fileUploadForm));
    verify(fileAssociationService).submitFiles(List.of(fileUploadForm));
    verify(nominationDetailService).updateNominationDetailStatusByDecision(nominationDetail, decision);
  }
}