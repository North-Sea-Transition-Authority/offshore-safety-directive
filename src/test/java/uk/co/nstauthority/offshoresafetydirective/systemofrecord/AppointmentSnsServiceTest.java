package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdTestUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.AppointmentCreatedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqTopics;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;

@ExtendWith(MockitoExtension.class)
class AppointmentSnsServiceTest {

  @Mock
  private SnsService snsService;

  @Mock
  private AppointmentRepository appointmentRepository;

  @Mock
  private AssetPhaseRepository assetPhaseRepository;

  private final SnsTopicArn appointmentsTopicArn = new SnsTopicArn("test-appointments-topic-arn");

  private final Instant instant = Instant.now();

  private AppointmentSnsService appointmentSnsService;

  @BeforeEach
  public void setUp() {
    when(snsService.getOrCreateTopic(OsdEpmqTopics.APPOINTMENTS.getName())).thenReturn(appointmentsTopicArn);

    appointmentSnsService = spy(
        new AppointmentSnsService(
            snsService,
            appointmentRepository,
            assetPhaseRepository,
            Clock.fixed(instant, ZoneId.systemDefault())
        )
    );
  }

  @Test
  void handleAppointmentConfirmed() {
    var nominationId = new NominationId(1);
    var event = new AppointmentConfirmedEvent(nominationId);

    doNothing().when(appointmentSnsService).publishAppointmentConfirmedSnsMessages(nominationId);

    appointmentSnsService.handleAppointmentConfirmed(event);

    verify(appointmentSnsService).publishAppointmentConfirmedSnsMessages(nominationId);
  }

  @Test
  void publishAppointmentConfirmedSnsMessages() {
    var nominationId = new NominationId(1);

    var correlationId = UUID.randomUUID().toString();

    CorrelationIdTestUtil.setCorrelationIdOnMdc(correlationId);

    var appointment1Asset = AssetTestUtil.builder().withId(UUID.randomUUID()).build();
    var appointment2Asset = AssetTestUtil.builder().withId(UUID.randomUUID()).build();

    var appointment1 = AppointmentTestUtil.builder().withAsset(appointment1Asset).build();
    var appointment2 = AppointmentTestUtil.builder().withAsset(appointment2Asset).build();

    var appointments = List.of(appointment1, appointment2);

    when(appointmentRepository.findAllByCreatedByNominationId(nominationId.id())).thenReturn(appointments);

    var appointment1AssetPhases = List.of(
        AssetPhaseTestUtil.builder().withAsset(appointment1Asset).withPhase("TEST_PHASE_1").build()
    );
    var appointment2AssetPhases = List.of(
        AssetPhaseTestUtil.builder().withAsset(appointment2Asset).withPhase("TEST_PHASE_2").build(),
        AssetPhaseTestUtil.builder().withAsset(appointment2Asset).withPhase("TEST_PHASE_3").build()
    );

    when(assetPhaseRepository.findByAsset_IdIn(Set.of(appointment1Asset.getId(), appointment2Asset.getId())))
        .thenReturn(Stream.concat(appointment1AssetPhases.stream(), appointment2AssetPhases.stream()).toList());

    doNothing().when(appointmentSnsService).publishAppointmentConfirmedSnsMessage(any(), any(), any());

    appointmentSnsService.publishAppointmentConfirmedSnsMessages(nominationId);

    verify(appointmentSnsService)
        .publishAppointmentConfirmedSnsMessage(appointment1, appointment1AssetPhases, correlationId);
    verify(appointmentSnsService)
        .publishAppointmentConfirmedSnsMessage(appointment2, appointment2AssetPhases, correlationId);
  }

  @Test
  void publishAppointmentConfirmedSnsMessage_withoutAssetPhases() {
    var correlationId = UUID.randomUUID().toString();

    CorrelationIdTestUtil.setCorrelationIdOnMdc(correlationId);

    var asset = AssetTestUtil.builder().withId(UUID.randomUUID()).build();

    var appointment = AppointmentTestUtil.builder().withAsset(asset).build();

    var assetPhases = List.of(
        AssetPhaseTestUtil.builder().withAsset(asset).withPhase("TEST_PHASE_1").build()
    );

    when(assetPhaseRepository.findByAsset_Id(asset.getId())).thenReturn(assetPhases);

    doNothing().when(appointmentSnsService).publishAppointmentConfirmedSnsMessage(any(), any(), any());

    appointmentSnsService.publishAppointmentConfirmedSnsMessage(appointment);

    verify(appointmentSnsService)
        .publishAppointmentConfirmedSnsMessage(appointment, assetPhases, correlationId);
  }

  @Test
  void publishAppointmentConfirmedSnsMessage_withAssetPhases() {
    var asset = AssetTestUtil.builder().withId(UUID.randomUUID()).build();
    var appointment = AppointmentTestUtil.builder().withAsset(asset).build();
    var assetPhases = List.of(
        AssetPhaseTestUtil.builder().withAsset(asset).withPhase("TEST_PHASE_1").build(),
        AssetPhaseTestUtil.builder().withAsset(asset).withPhase("TEST_PHASE_2").build()
    );
    var correlationId = UUID.randomUUID().toString();

    appointmentSnsService.publishAppointmentConfirmedSnsMessage(appointment, assetPhases, correlationId);

    var epmqMessageArgumentCaptor = ArgumentCaptor.forClass(AppointmentCreatedOsdEpmqMessage.class);

    verify(snsService).publishMessage(eq(appointmentsTopicArn), epmqMessageArgumentCaptor.capture());

    var epmqMessage = epmqMessageArgumentCaptor.getValue();

    assertThat(epmqMessage).isNotNull();
    assertThat(epmqMessage.getAppointmentId()).isEqualTo(appointment.getId());
    assertThat(epmqMessage.getPortalAssetId()).isEqualTo(asset.getPortalAssetId());
    assertThat(epmqMessage.getPortalAssetType()).isEqualTo(asset.getPortalAssetType().name());
    assertThat(epmqMessage.getAppointedPortalOperatorId()).isEqualTo(appointment.getAppointedPortalOperatorId());
    assertThat(epmqMessage.getPhases()).isEqualTo(assetPhases.stream().map(AssetPhase::getPhase).toList());
    assertThat(epmqMessage.getCorrelationId()).isEqualTo(correlationId);
    assertThat(epmqMessage.getCreatedInstant()).isEqualTo(instant);
  }
}
