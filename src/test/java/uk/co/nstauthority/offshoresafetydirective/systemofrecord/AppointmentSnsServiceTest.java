package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  private AppointmentSnsService appointmentSnsService;

  @BeforeEach
  public void setUp() {
    when(snsService.getOrCreateTopic(AppointmentSnsService.APPOINTMENTS_TOPIC_NAME)).thenReturn(appointmentsTopicArn);

    appointmentSnsService = spy(new AppointmentSnsService(snsService, appointmentRepository, assetPhaseRepository));
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

    appointmentSnsService.publishAppointmentConfirmedSnsMessages(nominationId);

    var epmqMessageArgumentCaptor = ArgumentCaptor.forClass(AppointmentCreatedOsdEpmqMessage.class);

    verify(snsService, times(2)).publishMessage(eq(appointmentsTopicArn), epmqMessageArgumentCaptor.capture());

    var epmqMessages = epmqMessageArgumentCaptor.getAllValues();

    assertThat(epmqMessages)
        .extracting(
            AppointmentCreatedOsdEpmqMessage::getAppointmentId,
            AppointmentCreatedOsdEpmqMessage::getPortalAssetId,
            AppointmentCreatedOsdEpmqMessage::getPortalAssetType,
            AppointmentCreatedOsdEpmqMessage::getAppointedPortalOperatorId,
            AppointmentCreatedOsdEpmqMessage::getPhases,
            AppointmentCreatedOsdEpmqMessage::getCorrelationId
        )
        .containsExactly(
            tuple(
                appointment1.getId(),
                appointment1Asset.getPortalAssetId(),
                appointment1Asset.getPortalAssetType().name(),
                appointment1.getAppointedPortalOperatorId(),
                appointment1AssetPhases.stream().map(AssetPhase::getPhase).toList(),
                correlationId
            ),
            tuple(
                appointment2.getId(),
                appointment2Asset.getPortalAssetId(),
                appointment2Asset.getPortalAssetType().name(),
                appointment2.getAppointedPortalOperatorId(),
                appointment2AssetPhases.stream().map(AssetPhase::getPhase).toList(),
                correlationId
            )
        );
  }
}
