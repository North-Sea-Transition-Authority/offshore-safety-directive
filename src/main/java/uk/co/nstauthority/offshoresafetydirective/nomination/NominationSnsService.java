package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.NominationSubmittedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqTopics;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailAccessService;

@Service
@Profile("!disable-epmq")
class NominationSnsService {

  private final SnsService snsService;
  private final SnsTopicArn nominationsTopicArn;
  private final ApplicantDetailAccessService applicantDetailAccessService;
  private final Clock clock;

  @Autowired
  NominationSnsService(SnsService snsService, ApplicantDetailAccessService applicantDetailAccessService, Clock clock) {
    this.snsService = snsService;
    nominationsTopicArn = snsService.getOrCreateTopic(OsdEpmqTopics.NOMINATIONS.getName());
    this.applicantDetailAccessService = applicantDetailAccessService;
    this.clock = clock;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNominationSubmitted(NominationSubmittedEvent event) {
    publishNominationSubmittedMessage(event.getNominationDetail());
  }

  void publishNominationSubmittedMessage(NominationDetail nominationDetail) {
    var nomination = nominationDetail.getNomination();
    var applicantOrganisationId = applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail)
        .orElseThrow(() ->
            new IllegalStateException(
                "Unable to find ApplicantDetailDto for NominationDetail %s".formatted(nominationDetail.getId())
            )
        )
        .applicantOrganisationId();
    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    snsService.publishMessage(
        nominationsTopicArn,
        new NominationSubmittedOsdEpmqMessage(
            nomination.getId(),
            nomination.getReference(),
            applicantOrganisationId.id(),
            correlationId,
            clock.instant()
        )
    );
  }
}
