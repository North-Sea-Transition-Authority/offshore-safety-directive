package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
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
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.AppointmentTerminationOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqTopics;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
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

  @Autowired
  AppointmentSnsService(
      SnsService snsService,
      AppointmentRepository appointmentRepository,
      AssetPhaseRepository assetPhaseRepository,
      Clock clock
  ) {
    this.snsService = snsService;
    appointmentsTopicArn = snsService.getOrCreateTopic(OsdEpmqTopics.APPOINTMENTS.getName());
    this.appointmentRepository = appointmentRepository;
    this.assetPhaseRepository = assetPhaseRepository;
    this.clock = clock;
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

    snsService.publishMessage(
        appointmentsTopicArn,
        new AppointmentTerminationOsdEpmqMessage(event.getAppointmentId().id(), correlationId, clock.instant())
    );
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleAppointmentAdded(ManualAppointmentAddedEvent event) {
    var appointment = appointmentRepository.findById(event.getAppointment().id())
        .orElseThrow(() -> new IllegalArgumentException(
            "No Appointment found with ID [%s]".formatted(
                event.getAppointment().id()
            )
        ));

    LOGGER.info("Received ManualAppointmentAddedEvent for appointment {}", appointment.getId());
    if (appointment.getResponsibleToDate() == null) {
      publishAppointmentCreatedSnsMessage(appointment);
    } else {
      LOGGER.info("AppointmentCreatedSnsMessage not published for appointment with id {} as is not active",
          event.getAppointment().id());
    }
  }

  void publishAppointmentCreatedSnsMessages(NominationId nominationId) {
    var appointments = appointmentRepository.findAllByCreatedByNominationId(nominationId.id());
    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    var assetIds = appointments.stream().map(appointment -> appointment.getAsset().getId()).collect(Collectors.toSet());
    var assetPhasesByAssetId = assetPhaseRepository.findByAsset_IdIn(assetIds).stream()
        .collect(Collectors.groupingBy(assetPhase -> assetPhase.getAsset().getId()));

    appointments.forEach(appointment -> {
      var asset = appointment.getAsset();
      var assetPhases = assetPhasesByAssetId.getOrDefault(asset.getId(), Collections.emptyList());

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
  }

  void publishAppointmentCreatedSnsMessage(Appointment appointment) {
    var assetPhases = assetPhaseRepository.findByAsset_Id(appointment.getAsset().getId());
    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    publishAppointmentCreatedSnsMessage(appointment, assetPhases, correlationId);
  }
}
