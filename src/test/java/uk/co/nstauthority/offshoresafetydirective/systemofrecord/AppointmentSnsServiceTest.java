package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.micrometer.core.instrument.Counter;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdTestUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.AppointmentCreatedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.AppointmentDeletedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.AppointmentUpdatedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqTopics;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionEvent;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationEvent;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective.systemofrecord",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@ExtendWith(MockitoExtension.class)
class AppointmentSnsServiceTest {

  private static final Instant FIXED_INSTANT = Instant.now();
  private static final String CORRELATION_ID = "1";

  @Mock
  private SnsService snsService;

  @Mock
  private AppointmentRepository appointmentRepository;

  @Mock
  private AssetPhaseRepository assetPhaseRepository;

  @Mock
  private MetricsProvider metricsProvider;

  private final SnsTopicArn appointmentsTopicArn = new SnsTopicArn("test-appointments-topic-arn");

  private AppointmentSnsService appointmentSnsService;

  private Counter counter;

  @BeforeEach
  public void setUp() {
    when(snsService.getOrCreateTopic(OsdEpmqTopics.APPOINTMENTS.getName())).thenReturn(appointmentsTopicArn);

    CorrelationIdTestUtil.setCorrelationIdOnMdc(CORRELATION_ID);

    counter = mock(Counter.class);

    appointmentSnsService = spy(
        new AppointmentSnsService(
            snsService,
            appointmentRepository,
            assetPhaseRepository,
            Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault()),
            metricsProvider)
    );
  }

  @ArchTest
  final ArchRule handleAppointmentConfirmed_isAsync = methods()
      .that()
      .areDeclaredIn(AppointmentSnsService.class)
      .and().haveName("handleAppointmentConfirmed")
      .should()
      .beAnnotatedWith(Async.class);

  @ArchTest
  final ArchRule handleAppointmentConfirmed_isTransactionalAfterCommit = methods()
      .that()
      .areDeclaredIn(AppointmentSnsService.class)
      .and().haveName("handleAppointmentConfirmed")
      .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

  @Test
  void handleAppointmentConfirmed() {
    var nominationId = new NominationId(UUID.randomUUID());
    var event = new AppointmentConfirmedEvent(nominationId);

    doNothing().when(appointmentSnsService).publishAppointmentCreatedSnsMessages(nominationId);

    appointmentSnsService.handleAppointmentConfirmed(event);

    verify(appointmentSnsService).publishAppointmentCreatedSnsMessages(nominationId);
  }

  @ArchTest
  final ArchRule handleAppointmentAdded_isAsync = methods()
      .that()
      .areDeclaredIn(AppointmentSnsService.class)
      .and().haveName("handleAppointmentAdded")
      .should()
      .beAnnotatedWith(Async.class);

  @ArchTest
  final ArchRule handleAppointmentAdded_isTransactionalAfterCommit = methods()
      .that()
      .areDeclaredIn(AppointmentSnsService.class)
      .and().haveName("handleAppointmentAdded")
      .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

  @Test
  void handleAppointmentAdded_whenCurrentAppointment() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = Optional.of(
        AppointmentTestUtil.builder().build()
    );

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(appointment);

    var event = new ManualAppointmentAddedEvent(appointmentId);

    doNothing().when(appointmentSnsService).publishAppointmentCreatedSnsMessage(appointment.get());

    appointmentSnsService.handleAppointmentAdded(event);

    verify(appointmentSnsService).publishAppointmentCreatedSnsMessage(appointment.get());
  }

  @Test
  void handleAppointmentAdded_whenNotCurrentAppointment() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = Optional.of(
        AppointmentTestUtil.builder()
            .withResponsibleToDate(LocalDate.now())
            .build()
    );

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(appointment);

    var event = new ManualAppointmentAddedEvent(appointmentId);

    appointmentSnsService.handleAppointmentAdded(event);

    verify(appointmentSnsService, never()).publishAppointmentCreatedSnsMessage(appointment.get());
  }

  @Test
  void handleAppointmentAdded_whenNoAppointment() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    when(appointmentRepository.findById(appointmentId.id())).thenReturn(Optional.empty());

    var event = new ManualAppointmentAddedEvent(appointmentId);

    assertThatThrownBy(() -> appointmentSnsService.handleAppointmentAdded(event))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No Appointment found with ID [%s]".formatted(appointmentId.id()));

    verify(metricsProvider, never()).getAppointmentsPublishedCounter();
    verify(counter, never()).increment();
  }

  @ArchTest
  final ArchRule handleAppointmentRemoved_isAsync = methods()
      .that()
      .areDeclaredIn(AppointmentSnsService.class)
      .and().haveName("handleAppointmentRemoved")
      .should()
      .beAnnotatedWith(Async.class);

  @ArchTest
  final ArchRule handleAppointmentRemoved_isTransactionalAfterCommit = methods()
      .that()
      .areDeclaredIn(AppointmentSnsService.class)
      .and().haveName("handleAppointmentRemoved")
      .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

  @Test
  void handleAppointmentRemoved_whenCurrentAppointment() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = Optional.of(
        AppointmentTestUtil.builder()
            .withId(appointmentId.id())
            .build()
    );

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(appointment);
    when(metricsProvider.getAppointmentsPublishedCounter()).thenReturn(counter);

    var event = new AppointmentRemovedEvent(appointmentId);

    appointmentSnsService.handleAppointmentRemoved(event);

    var argumentCaptor = ArgumentCaptor.forClass(AppointmentDeletedOsdEpmqMessage.class);

    verify(snsService).publishMessage(eq(appointmentsTopicArn), argumentCaptor.capture());
    verify(metricsProvider).getAppointmentsPublishedCounter();
    verify(counter).increment();

    assertThat(argumentCaptor.getValue())
        .extracting(
            AppointmentDeletedOsdEpmqMessage::getAppointmentId,
            EpmqMessage::getCreatedInstant,
            EpmqMessage::getCorrelationId
        )
        .containsExactly(
            appointmentId.id(),
            FIXED_INSTANT,
            CORRELATION_ID
        );
  }

  @Test
  void handleAppointmentRemoved_whenNotCurrentAppointment() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = Optional.of(
        AppointmentTestUtil.builder()
            .withId(appointmentId.id())
            .withResponsibleToDate(LocalDate.now())
            .build()
    );

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(appointment);
    var event = new AppointmentRemovedEvent(appointmentId);

    appointmentSnsService.handleAppointmentRemoved(event);

    verify(snsService, never()).publishMessage(any(), any());
    verify(metricsProvider, never()).getAppointmentsPublishedCounter();
    verify(counter, never()).increment();
  }

  @Test
  void handleAppointmentRemoved_whenNoAppointment() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    when(appointmentRepository.findById(appointmentId.id())).thenReturn(Optional.empty());
    var event = new AppointmentRemovedEvent(appointmentId);

    assertThatThrownBy(() -> appointmentSnsService.handleAppointmentRemoved(event))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No Appointment found with ID [%s]".formatted(appointmentId.id()));

    verify(metricsProvider, never()).getAppointmentsPublishedCounter();
    verify(counter, never()).increment();
  }

  @ArchTest
  final ArchRule handleAppointmentTermination_isAsync = methods()
      .that()
      .areDeclaredIn(AppointmentSnsService.class)
      .and().haveName("handleAppointmentTermination")
      .should()
      .beAnnotatedWith(Async.class);

  @ArchTest
  final ArchRule handleAppointmentTermination_isTransactionalAfterCommit = methods()
      .that()
      .areDeclaredIn(AppointmentSnsService.class)
      .and().haveName("handleAppointmentTermination")
      .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

  @Test
  void handleAppointmentTermination() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var event = new AppointmentTerminationEvent(appointmentId);

    when(metricsProvider.getAppointmentsPublishedCounter()).thenReturn(counter);

    appointmentSnsService.handleAppointmentTermination(event);

    var argumentCaptor = ArgumentCaptor.forClass(AppointmentDeletedOsdEpmqMessage.class);

    verify(snsService).publishMessage(eq(appointmentsTopicArn), argumentCaptor.capture());
    verify(metricsProvider).getAppointmentsPublishedCounter();
    verify(counter).increment();

    assertThat(argumentCaptor.getValue())
        .extracting(
            AppointmentDeletedOsdEpmqMessage::getAppointmentId,
            EpmqMessage::getCreatedInstant,
            EpmqMessage::getCorrelationId
        )
        .containsExactly(
            appointmentId.id(),
            FIXED_INSTANT,
            CORRELATION_ID
        );
  }

  @ArchTest
  final ArchRule handleAppointmentCorrected_isAsync = methods()
      .that()
      .areDeclaredIn(AppointmentSnsService.class)
      .and().haveName("handleAppointmentCorrected")
      .should()
      .beAnnotatedWith(Async.class);

  @ArchTest
  final ArchRule handleAppointmentCorrected_isTransactionalAfterCommit = methods()
      .that()
      .areDeclaredIn(AppointmentSnsService.class)
      .and().haveName("handleAppointmentCorrected")
      .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

  @Test
  void handleAppointmentCorrected_whenCurrentAppointment_thenPublishUpdateMessage() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var asset = AssetTestUtil.builder().withId(UUID.randomUUID()).build();
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(appointmentId.id())
        .build();
    var assetPhases = List.of(
        AssetPhaseTestUtil.builder().withAsset(asset).withPhase("TEST_PHASE_1").build(),
        AssetPhaseTestUtil.builder().withAsset(asset).withPhase("TEST_PHASE_2").build()
    );

    when(assetPhaseRepository.findByAppointment( appointment))
        .thenReturn(assetPhases);

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(Optional.of(appointment));
    when(metricsProvider.getAppointmentsPublishedCounter()).thenReturn(counter);

    var event = new AppointmentCorrectionEvent(appointmentId);

    appointmentSnsService.handleAppointmentCorrected(event);

    var argumentCaptor = ArgumentCaptor.forClass(AppointmentUpdatedOsdEpmqMessage.class);

    verify(snsService).publishMessage(eq(appointmentsTopicArn), argumentCaptor.capture());
    verify(metricsProvider).getAppointmentsPublishedCounter();
    verify(counter).increment();

    var epmqMessage = argumentCaptor.getValue();

    assertThat(epmqMessage).isNotNull();
    assertThat(epmqMessage.getAppointmentId()).isEqualTo(appointment.getId());
    assertThat(epmqMessage.getPortalAssetId()).isEqualTo(asset.getPortalAssetId());
    assertThat(epmqMessage.getPortalAssetType()).isEqualTo(asset.getPortalAssetType().name());
    assertThat(epmqMessage.getAppointedPortalOperatorId()).isEqualTo(appointment.getAppointedPortalOperatorId());
    assertThat(epmqMessage.getPhases()).isEqualTo(assetPhases.stream().map(AssetPhase::getPhase).toList());
    assertThat(epmqMessage.getCorrelationId()).isEqualTo(CORRELATION_ID);
    assertThat(epmqMessage.getCreatedInstant()).isEqualTo(FIXED_INSTANT);
  }

  @Test
  void handleAppointmentCorrected_whenEndedAppointment_thenPublishDeleteMessage() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = Optional.of(
        AppointmentTestUtil.builder()
            .withId(appointmentId.id())
            .withResponsibleToDate(LocalDate.now())
            .build()
    );

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(appointment);
    when(metricsProvider.getAppointmentsPublishedCounter()).thenReturn(counter);

    var event = new AppointmentCorrectionEvent(appointmentId);

    appointmentSnsService.handleAppointmentCorrected(event);

    var argumentCaptor = ArgumentCaptor.forClass(AppointmentDeletedOsdEpmqMessage.class);

    verify(snsService).publishMessage(eq(appointmentsTopicArn), argumentCaptor.capture());
    verify(metricsProvider).getAppointmentsPublishedCounter();
    verify(counter).increment();

    assertThat(argumentCaptor.getValue())
        .extracting(
            AppointmentDeletedOsdEpmqMessage::getAppointmentId,
            EpmqMessage::getCreatedInstant,
            EpmqMessage::getCorrelationId
        )
        .containsExactly(
            appointmentId.id(),
            FIXED_INSTANT,
            CORRELATION_ID
        );
  }

  @Test
  void publishAppointmentCreatedSnsMessages() {
    var nominationId = new NominationId(UUID.randomUUID());

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

    when(assetPhaseRepository.findByAppointmentIn(List.of(appointment1, appointment2)))
        .thenReturn(Stream.concat(appointment1AssetPhases.stream(), appointment2AssetPhases.stream()).toList());

    doNothing().when(appointmentSnsService).publishAppointmentCreatedSnsMessage(any(), any(), any());

    appointmentSnsService.publishAppointmentCreatedSnsMessages(nominationId);

    verify(appointmentSnsService)
        .publishAppointmentCreatedSnsMessage(appointment1, appointment1AssetPhases, correlationId);
    verify(appointmentSnsService)
        .publishAppointmentCreatedSnsMessage(appointment2, appointment2AssetPhases, correlationId);
  }

  @Test
  void publishAppointmentCreatedSnsMessage_withoutAssetPhases() {
    var correlationId = UUID.randomUUID().toString();

    CorrelationIdTestUtil.setCorrelationIdOnMdc(correlationId);

    var asset = AssetTestUtil.builder().withId(UUID.randomUUID()).build();

    var appointment = AppointmentTestUtil.builder().withAsset(asset).build();

    var assetPhases = List.of(
        AssetPhaseTestUtil.builder().withAsset(asset).withPhase("TEST_PHASE_1").build()
    );

    when(assetPhaseRepository.findByAppointment(appointment)).thenReturn(assetPhases);

    doNothing().when(appointmentSnsService).publishAppointmentCreatedSnsMessage(any(), any(), any());

    appointmentSnsService.publishAppointmentCreatedSnsMessage(appointment);

    verify(appointmentSnsService)
        .publishAppointmentCreatedSnsMessage(appointment, assetPhases, correlationId);
  }

  @Test
  void publishAppointmentCreatedSnsMessage_withAssetPhases() {
    var asset = AssetTestUtil.builder().withId(UUID.randomUUID()).build();
    var appointment = AppointmentTestUtil.builder().withAsset(asset).build();
    var assetPhases = List.of(
        AssetPhaseTestUtil.builder().withAsset(asset).withPhase("TEST_PHASE_1").build(),
        AssetPhaseTestUtil.builder().withAsset(asset).withPhase("TEST_PHASE_2").build()
    );

    when(metricsProvider.getAppointmentsPublishedCounter()).thenReturn(counter);

    appointmentSnsService.publishAppointmentCreatedSnsMessage(appointment, assetPhases, CORRELATION_ID);

    var epmqMessageArgumentCaptor = ArgumentCaptor.forClass(AppointmentCreatedOsdEpmqMessage.class);

    verify(snsService).publishMessage(eq(appointmentsTopicArn), epmqMessageArgumentCaptor.capture());

    var epmqMessage = epmqMessageArgumentCaptor.getValue();

    assertThat(epmqMessage).isNotNull();
    assertThat(epmqMessage.getAppointmentId()).isEqualTo(appointment.getId());
    assertThat(epmqMessage.getPortalAssetId()).isEqualTo(asset.getPortalAssetId());
    assertThat(epmqMessage.getPortalAssetType()).isEqualTo(asset.getPortalAssetType().name());
    assertThat(epmqMessage.getAppointedPortalOperatorId()).isEqualTo(appointment.getAppointedPortalOperatorId());
    assertThat(epmqMessage.getPhases()).isEqualTo(assetPhases.stream().map(AssetPhase::getPhase).toList());
    assertThat(epmqMessage.getCorrelationId()).isEqualTo(CORRELATION_ID);
    assertThat(epmqMessage.getCreatedInstant()).isEqualTo(FIXED_INSTANT);

    verify(metricsProvider).getAppointmentsPublishedCounter();
    verify(counter).increment();
  }

  @Test
  void publishAppointmentUpdatedSnsMessage() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var asset = AssetTestUtil.builder().withId(UUID.randomUUID()).build();
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(appointmentId.id())
        .build();
    var assetPhases = List.of(
        AssetPhaseTestUtil.builder().withAsset(asset).withPhase("TEST_PHASE_1").build(),
        AssetPhaseTestUtil.builder().withAsset(asset).withPhase("TEST_PHASE_2").build()
    );

    when(assetPhaseRepository.findByAppointment( appointment))
        .thenReturn(assetPhases);

    appointmentSnsService.publishAppointmentUpdatedSnsMessage(appointment, CORRELATION_ID);

    var argumentCaptor = ArgumentCaptor.forClass(AppointmentUpdatedOsdEpmqMessage.class);

    verify(snsService).publishMessage(eq(appointmentsTopicArn), argumentCaptor.capture());

    var epmqMessage = argumentCaptor.getValue();

    assertThat(epmqMessage).isNotNull();
    assertThat(epmqMessage.getAppointmentId()).isEqualTo(appointment.getId());
    assertThat(epmqMessage.getPortalAssetId()).isEqualTo(asset.getPortalAssetId());
    assertThat(epmqMessage.getPortalAssetType()).isEqualTo(asset.getPortalAssetType().name());
    assertThat(epmqMessage.getAppointedPortalOperatorId()).isEqualTo(appointment.getAppointedPortalOperatorId());
    assertThat(epmqMessage.getPhases()).isEqualTo(assetPhases.stream().map(AssetPhase::getPhase).toList());
    assertThat(epmqMessage.getCorrelationId()).isEqualTo(CORRELATION_ID);
    assertThat(epmqMessage.getCreatedInstant()).isEqualTo(FIXED_INSTANT);
  }

  @Test
  void publishAppointmentDeletedSnsMessage() {
    var appointmentId = UUID.randomUUID();

    var argumentCaptor = ArgumentCaptor.forClass(AppointmentDeletedOsdEpmqMessage.class);

    appointmentSnsService.publishAppointmentDeletedSnsMessage(appointmentId, CORRELATION_ID);

    verify(snsService).publishMessage(eq(appointmentsTopicArn), argumentCaptor.capture());

    var epmqMessage = argumentCaptor.getValue();

    assertThat(epmqMessage).isNotNull();
    assertThat(epmqMessage.getAppointmentId()).isEqualTo(appointmentId);
    assertThat(epmqMessage.getCorrelationId()).isEqualTo(CORRELATION_ID);
    assertThat(epmqMessage.getCreatedInstant()).isEqualTo(FIXED_INSTANT);
  }
}
