package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@Service
class NominationRequestUpdateSubmissionService {

  private final CaseEventService caseEventService;
  private final NominationUpdateRequestedEventPublisher nominationUpdateRequestedEventPublisher;

  @Autowired
  NominationRequestUpdateSubmissionService(CaseEventService caseEventService,
                                           NominationUpdateRequestedEventPublisher nominationUpdateRequestedEventPublisher) {
    this.caseEventService = caseEventService;
    this.nominationUpdateRequestedEventPublisher = nominationUpdateRequestedEventPublisher;
  }

  @Transactional
  public void submit(NominationDetail nominationDetail, NominationRequestUpdateForm form) {
    caseEventService.createUpdateRequestEvent(nominationDetail, form.getReason().getInputValue());
    var nominationId = new NominationId(nominationDetail);
    nominationUpdateRequestedEventPublisher.publish(nominationId);
  }
}
