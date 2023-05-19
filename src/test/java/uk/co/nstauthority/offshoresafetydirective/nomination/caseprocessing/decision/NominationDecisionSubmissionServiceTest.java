package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import static org.mockito.BDDMockito.then;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

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

  @Mock
  private NominationDecisionDeterminedEventPublisher nominationDecisionDeterminedEventPublisher;

  @InjectMocks
  private NominationDecisionSubmissionService nominationDecisionSubmissionService;

  @Test
  void submitNominationDecision_verifyCalls() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var date = LocalDate.now();
    var comment = "comment";
    var decision = NominationDecision.NO_OBJECTION;
    var fileUploadForm = new FileUploadForm();

    var form = new NominationDecisionForm();
    form.setNominationDecision(decision);
    form.setDecisionFiles(List.of(fileUploadForm));
    form.getDecisionDate().setDate(date);
    form.getComments().setInputValue(comment);

    nominationDecisionSubmissionService.submitNominationDecision(nominationDetail, form);

    then(caseEventService)
        .should(onlyOnce())
        .createDecisionEvent(nominationDetail, date, comment, decision, List.of(fileUploadForm));

    then(fileUploadService)
        .should(onlyOnce())
        .updateFileUploadDescriptions(List.of(fileUploadForm));

    then(fileAssociationService)
        .should(onlyOnce())
        .submitFiles(List.of(fileUploadForm));

    then(nominationDetailService)
        .should(onlyOnce())
        .updateNominationDetailStatusByDecision(nominationDetail, decision);

    then(nominationDecisionDeterminedEventPublisher)
        .should(onlyOnce())
        .publish(new NominationId(nominationDetail.getNomination().getId()));
  }
}