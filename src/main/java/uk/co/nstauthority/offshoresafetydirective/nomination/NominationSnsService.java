package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.NominationSubmittedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsService;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;

@Service
class NominationSnsService {

  static final String NOMINATIONS_TOPIC_NAME = "osd-nominations";

  private final SnsService snsService;
  private final SnsTopicArn nominationsTopicArn;
  private final ApplicantDetailAccessService applicantDetailAccessService;

  @Autowired
  NominationSnsService(SnsService snsService, ApplicantDetailAccessService applicantDetailAccessService) {
    this.snsService = snsService;
    nominationsTopicArn = snsService.getOrCreateTopic(NOMINATIONS_TOPIC_NAME);
    this.applicantDetailAccessService = applicantDetailAccessService;
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
            correlationId
        )
    );
  }
}
