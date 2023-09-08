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
  private final NominationDetailService nominationDetailService;
  private final Clock clock;

  @Autowired
  NominationSnsService(SnsService snsService, ApplicantDetailAccessService applicantDetailAccessService,
                       NominationDetailService nominationDetailService, Clock clock) {
    this.snsService = snsService;
    nominationsTopicArn = snsService.getOrCreateTopic(OsdEpmqTopics.NOMINATIONS.getName());
    this.applicantDetailAccessService = applicantDetailAccessService;
    this.nominationDetailService = nominationDetailService;
    this.clock = clock;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNominationSubmitted(NominationSubmittedEvent event) {
    publishNominationSubmittedMessage(event.getNominationId());
  }

  void publishNominationSubmittedMessage(NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    var nomination = nominationDetail.getNomination();
    var applicantOrganisationId = applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail)
        .orElseThrow(() ->
            new IllegalStateException(
                "Unable to find ApplicantDetailDto for NominationDetail %s".formatted(nominationId.id())
            )
        )
        .applicantOrganisationId();
    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    snsService.publishMessage(
        nominationsTopicArn,
        new NominationSubmittedOsdEpmqMessage(
            nominationId.id(),
            nomination.getReference(),
            applicantOrganisationId.id(),
            correlationId,
            clock.instant()
        )
    );
  }
}
