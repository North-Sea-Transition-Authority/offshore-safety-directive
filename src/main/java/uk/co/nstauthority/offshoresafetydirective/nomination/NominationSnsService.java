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
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominationHasInstallations;

@Service
@Profile("!disable-epmq")
class NominationSnsService {

  private final SnsService snsService;
  private final SnsTopicArn nominationsTopicArn;
  private final NominationDetailService nominationDetailService;
  private final NominationSnsQueryService nominationSnsQueryService;
  private final Clock clock;

  @Autowired
  NominationSnsService(SnsService snsService, NominationDetailService nominationDetailService,
                       NominationSnsQueryService nominationSnsQueryService, Clock clock) {
    this.snsService = snsService;
    nominationsTopicArn = snsService.getOrCreateTopic(OsdEpmqTopics.NOMINATIONS.getName());
    this.nominationDetailService = nominationDetailService;
    this.nominationSnsQueryService = nominationSnsQueryService;
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

    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    var snsDto = nominationSnsQueryService.getNominationSnsDto(nominationDetail);

    var empqMessage = NominationSubmittedOsdEpmqMessage.builder(correlationId, clock.instant())
        .withNominationId(nomination.getId())
        .withNominationReference(nomination.getReference())
        .withApplicantOrganisationUnitId(snsDto.applicantOrganisationUnitId())
        .withNominatedOrganisationUnitId(snsDto.nominatedOrganisationUnitId())
        .withNominationAssetType(NominationDisplayType.getByWellSelectionTypeAndHasInstallations(
            snsDto.wellSelectionType(),
            NominationHasInstallations.fromBoolean(snsDto.hasInstallations())
        ))
        .build();

    snsService.publishMessage(
        nominationsTopicArn,
        empqMessage
    );
  }
}
