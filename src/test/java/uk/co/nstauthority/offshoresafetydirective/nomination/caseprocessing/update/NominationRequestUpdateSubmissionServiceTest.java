package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@ExtendWith(MockitoExtension.class)
class NominationRequestUpdateSubmissionServiceTest {

  @Mock
  private CaseEventService caseEventService;

  @Mock
  private NominationUpdateRequestedEventPublisher nominationUpdateRequestedEventPublisher;

  @InjectMocks
  private NominationRequestUpdateSubmissionService nominationRequestUpdateSubmissionService;

  @Test
  void submit_verifyCalls() {
    var reason = "reason";
    var form = new NominationRequestUpdateForm();
    form.getReason().setInputValue(reason);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    nominationRequestUpdateSubmissionService.submit(nominationDetail, form);

    verify(caseEventService).createUpdateRequestEvent(nominationDetail, reason);
    verify(nominationUpdateRequestedEventPublisher).publish(new NominationId(nominationDetail));
  }
}