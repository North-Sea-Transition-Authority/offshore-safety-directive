package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.NominationSubmittedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsService;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;

@Service
class NominationSnsService {

  static final String NOMINATIONS_TOPIC_NAME = "osd-nominations";

  private final SnsService snsService;
  private final SnsTopicArn nominationsTopicArn;

  @Autowired
  NominationSnsService(SnsService snsService) {
    this.snsService = snsService;

    nominationsTopicArn = snsService.getOrCreateTopic(NOMINATIONS_TOPIC_NAME);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNominationSubmitted(NominationSubmittedEvent event) {
    publishNominationSubmittedMessage(event.getNominationDetail());
  }

  void publishNominationSubmittedMessage(NominationDetail nominationDetail) {
    var nominationId = nominationDetail.getNomination().getId();
    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    snsService.publishMessage(nominationsTopicArn, new NominationSubmittedOsdEpmqMessage(nominationId, correlationId));
  }
}
