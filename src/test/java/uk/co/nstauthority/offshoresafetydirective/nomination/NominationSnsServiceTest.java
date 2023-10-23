package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdTestUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.NominationSubmittedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqTopics;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

@ExtendWith(MockitoExtension.class)
class NominationSnsServiceTest {

  @Mock
  private SnsService snsService;

  @Mock
  private NominationSnsQueryService nominationSnsQueryService;

  @Mock
  private NominationDetailService nominationDetailService;

  @Mock
  private MetricsProvider metricsProvider;

  private final SnsTopicArn nominationsTopicArn = new SnsTopicArn("test-nominations-topic-arn");

  private final Instant instant = Instant.now();

  private NominationSnsService nominationSnsService;

  private Counter counter;

  @BeforeEach
  void setUp() {
    when(snsService.getOrCreateTopic(OsdEpmqTopics.NOMINATIONS.getName())).thenReturn(nominationsTopicArn);

    counter = mock(Counter.class);

    nominationSnsService = spy(
        new NominationSnsService(
            snsService,
            nominationDetailService,
            nominationSnsQueryService,
            Clock.fixed(instant, ZoneId.systemDefault()),
            metricsProvider)
    );
  }

  @Test
  void handleNominationSubmitted() {
    var event = NominationSubmittedEventTestUtil.createEvent(new NominationId(UUID.randomUUID()));

    doNothing().when(nominationSnsService).publishNominationSubmittedMessage(event.getNominationId());

    nominationSnsService.handleNominationSubmitted(event);

    verify(nominationSnsService).publishNominationSubmittedMessage(event.getNominationId());
  }

  @Test
  void publishNominationSubmittedMessage() {
    var nominationId = new NominationId(UUID.randomUUID());
    var nomination = NominationTestUtil.builder()
        .withId(nominationId.id())
        .build();
    var nominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .build();
    var correlationId = UUID.randomUUID().toString();

    CorrelationIdTestUtil.setCorrelationIdOnMdc(correlationId);

    var nominationDetailId = new NominationDetailId(nominationDetail.getId());
    var applicantOrgId = 123;
    var nominatedOrgId = 456;
    var hasInstallations = true;
    var wellSelectionType = WellSelectionType.NO_WELLS;

    var snsDto = NominationSnsDtoTestUtil.builder()
        .withNominationDetailId(nominationDetailId)
        .withHasInstallations(hasInstallations)
        .withApplicantOrganisationUnitId(new ApplicantOrganisationId(applicantOrgId))
        .withNominatedOrganisationUnitId(new NominatedOrganisationId(nominatedOrgId))
        .withWellSelectionType(wellSelectionType)
        .build();
    when(nominationSnsQueryService.getNominationSnsDto(nominationDetail))
        .thenReturn(snsDto);

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    when(metricsProvider.getNominationsPublishedCounter()).thenReturn(counter);

    nominationSnsService.publishNominationSubmittedMessage(nominationId);

    var epmqMessageArgumentCaptor = ArgumentCaptor.forClass(NominationSubmittedOsdEpmqMessage.class);

    verify(snsService).publishMessage(eq(nominationsTopicArn), epmqMessageArgumentCaptor.capture());

    var epmqMessage = epmqMessageArgumentCaptor.getValue();

    assertThat(epmqMessage)
        .extracting(
            NominationSubmittedOsdEpmqMessage::getNominationId,
            NominationSubmittedOsdEpmqMessage::getNominationReference,
            NominationSubmittedOsdEpmqMessage::getApplicantOrganisationUnitId,
            NominationSubmittedOsdEpmqMessage::getNominatedOrganisationUnitId,
            NominationSubmittedOsdEpmqMessage::getNominationAssetType,
            NominationSubmittedOsdEpmqMessage::getCorrelationId,
            NominationSubmittedOsdEpmqMessage::getCreatedInstant
        )
        .containsExactly(
            nomination.getId(),
            nomination.getReference(),
            applicantOrgId,
            nominatedOrgId,
            NominationDisplayType.INSTALLATION,
            correlationId,
            instant
        );

    verify(metricsProvider).getNominationsPublishedCounter();
    verify(counter).increment();
  }
}
