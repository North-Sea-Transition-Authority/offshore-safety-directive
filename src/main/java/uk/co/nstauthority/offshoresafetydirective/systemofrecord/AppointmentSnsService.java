package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.AppointmentCreatedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.AppointmentDeletedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.AppointmentUpdatedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqTopics;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionEvent;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.message.ended.AppointmentEndedEvent;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationEvent;

@Service
@Profile("!disable-epmq")
class AppointmentSnsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentSnsService.class);
  private final SnsService snsService;
  private final SnsTopicArn appointmentsTopicArn;
  private final AppointmentRepository appointmentRepository;
  private final AssetPhaseRepository assetPhaseRepository;
  private final Clock clock;
  private final MetricsProvider metricsProvider;

  @Autowired
  AppointmentSnsService(
      SnsService snsService,
      AppointmentRepository appointmentRepository,
      AssetPhaseRepository assetPhaseRepository,
      Clock clock,
      MetricsProvider metricsProvider) {
    this.snsService = snsService;
    appointmentsTopicArn = snsService.getOrCreateTopic(OsdEpmqTopics.APPOINTMENTS.getName());
    this.appointmentRepository = appointmentRepository;
    this.assetPhaseRepository = assetPhaseRepository;
    this.clock = clock;
    this.metricsProvider = metricsProvider;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleAppointmentConfirmed(AppointmentConfirmedEvent event) {
    LOGGER.info("Received AppointmentConfirmedEvent for nomination {}", event.getNominationId().id());

    publishAppointmentCreatedSnsMessages(event.getNominationId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleAppointmentTermination(AppointmentTerminationEvent event) {
    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    LOGGER.info("Received AppointmentTerminationEvent for appointment {}", event.getAppointmentId().id());

    publishAppointmentDeletedSnsMessage(event.getAppointmentId().id(), correlationId);

    metricsProvider.getAppointmentsPublishedCounter().increment();
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleAppointmentAdded(ManualAppointmentAddedEvent event) {
    var appointment = getAppointment(event.getAppointment().id());
    LOGGER.info("Received ManualAppointmentAddedEvent for appointment {}", appointment.getId());

    if (appointment.getResponsibleToDate() == null) {
      publishAppointmentCreatedSnsMessage(appointment);
    } else {
      LOGGER.info("AppointmentCreatedSnsMessage not published for appointment with id {} as is not active",
          event.getAppointment().id());
    }
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleAppointmentRemoved(AppointmentRemovedEvent event) {
    var appointment = getAppointment(event.getAppointment().id());
    LOGGER.info("Received AppointmentRemovedEvent for appointment {}", event.getAppointment().id());

    if (appointment.getResponsibleToDate() == null) {
      var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();
      publishAppointmentDeletedSnsMessage(appointment.getId(), correlationId);
      metricsProvider.getAppointmentsPublishedCounter().increment();
    } else {
      LOGGER.info("AppointmentDeletedOsdEpmqMessage not published for appointment with id {} as is not active",
          event.getAppointment().id());
    }
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleAppointmentCorrected(AppointmentCorrectionEvent event) {
    var appointment = getAppointment(event.getAppointment().id());
    LOGGER.info("Received AppointmentCorrectionEvent for appointment {}", event.getAppointment().id());

    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();
    if (appointment.getResponsibleToDate() == null) {
      publishAppointmentUpdatedSnsMessage(appointment, correlationId);

    } else {
      publishAppointmentDeletedSnsMessage(appointment.getId(), correlationId);
    }
    metricsProvider.getAppointmentsPublishedCounter().increment();
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleAppointmentEnded(AppointmentEndedEvent event) {

    var appointment = getAppointment(event.getAppointmentId());

    LOGGER.info("Received AppointmentEndedEvent for appointment with ID {}", event.getAppointmentId());

    if (appointment.getResponsibleToDate() == null) {
      throw new IllegalStateException("""
          Received AppointmentEndedEvent for appointment with ID %s but appointment is active. An incorrect event
          has been emitted. No message has been sent to the portal message queue."""
          .formatted(event.getAppointmentId()));
    }

    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    publishAppointmentDeletedSnsMessage(appointment.getId(), correlationId);

    metricsProvider.getAppointmentsPublishedCounter().increment();
  }

  void publishAppointmentDeletedSnsMessage(UUID appointmentId, String correlationId) {
    snsService.publishMessage(
        appointmentsTopicArn,
        new AppointmentDeletedOsdEpmqMessage(appointmentId, correlationId, clock.instant())
    );
  }

  void publishAppointmentUpdatedSnsMessage(Appointment appointment, String correlationId) {
    var assetPhasesByAssetId =
        assetPhaseRepository.findByAppointment(appointment)
            .stream()
            .collect(Collectors.groupingBy(assetPhase -> assetPhase.getAsset().getId()));

    var asset = appointment.getAsset();
    var assetPhases = assetPhasesByAssetId.getOrDefault(asset.getId(), Collections.emptyList())
        .stream()
        .map(AssetPhase::getPhase)
        .toList();

    snsService.publishMessage(
        appointmentsTopicArn,
        new AppointmentUpdatedOsdEpmqMessage(
            appointment.getId(),
            asset.getPortalAssetId(),
            asset.getPortalAssetType().name(),
            appointment.getAppointedPortalOperatorId(),
            assetPhases,
            correlationId,
            clock.instant())
    );
  }

  void publishAppointmentCreatedSnsMessages(NominationId nominationId) {
    var appointments = appointmentRepository.findAllByCreatedByNominationId(nominationId.id());
    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    var assetPhasesByAssetIdAndAppointment =
        assetPhaseRepository.findByAppointmentIn(appointments).stream()
            .collect(Collectors.groupingBy(assetPhase -> assetPhase.getAsset().getId()));

    appointments.forEach(appointment -> {
      var asset = appointment.getAsset();
      var assetPhases = assetPhasesByAssetIdAndAppointment.getOrDefault(asset.getId(), Collections.emptyList());

      publishAppointmentCreatedSnsMessage(appointment, assetPhases, correlationId);
    });
  }

  void publishAppointmentCreatedSnsMessage(
      Appointment appointment,
      List<AssetPhase> assetPhases,
      String correlationId
  ) {
    var asset = appointment.getAsset();

    snsService.publishMessage(
        appointmentsTopicArn,
        new AppointmentCreatedOsdEpmqMessage(
            appointment.getId(),
            asset.getPortalAssetId(),
            asset.getPortalAssetType().name(),
            appointment.getAppointedPortalOperatorId(),
            assetPhases.stream().map(AssetPhase::getPhase).toList(),
            correlationId,
            clock.instant()
        )
    );
    metricsProvider.getAppointmentsPublishedCounter().increment();
  }

  void publishAppointmentCreatedSnsMessage(Appointment appointment) {
    var assetPhasesByAssetAndAppointment =
        assetPhaseRepository.findByAppointment(appointment)
            .stream()
            .collect(Collectors.groupingBy(assetPhase -> assetPhase.getAsset().getId()));

    var asset = appointment.getAsset();
    var assetPhases = assetPhasesByAssetAndAppointment.getOrDefault(asset.getId(), Collections.emptyList());
    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    publishAppointmentCreatedSnsMessage(appointment, assetPhases, correlationId);
  }

  private Appointment getAppointment(UUID appointmentId) {
    return appointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new IllegalArgumentException(
            "No Appointment found with ID [%s]".formatted(appointmentId)
        ));
  }
}
