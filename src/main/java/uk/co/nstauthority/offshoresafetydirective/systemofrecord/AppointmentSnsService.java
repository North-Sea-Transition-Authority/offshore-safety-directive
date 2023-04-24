package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqTopics;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;

@Service
@Profile("!disable-epmq")
class AppointmentSnsService {

  private final SnsService snsService;
  private final SnsTopicArn appointmentsTopicArn;
  private final AppointmentRepository appointmentRepository;
  private final AssetPhaseRepository assetPhaseRepository;

  @Autowired
  AppointmentSnsService(
      SnsService snsService,
      AppointmentRepository appointmentRepository,
      AssetPhaseRepository assetPhaseRepository
  ) {
    this.snsService = snsService;
    appointmentsTopicArn = snsService.getOrCreateTopic(OsdEpmqTopics.APPOINTMENTS.getName());
    this.appointmentRepository = appointmentRepository;
    this.assetPhaseRepository = assetPhaseRepository;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleAppointmentConfirmed(AppointmentConfirmedEvent event) {
    publishAppointmentConfirmedSnsMessages(event.getNominationId());
  }

  void publishAppointmentConfirmedSnsMessages(NominationId nominationId) {
    var appointments = appointmentRepository.findAllByCreatedByNominationId(nominationId.id());
    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    var assetIds = appointments.stream().map(appointment -> appointment.getAsset().getId()).collect(Collectors.toSet());
    var assetPhasesByAssetId = assetPhaseRepository.findByAsset_IdIn(assetIds).stream()
        .collect(Collectors.groupingBy(assetPhase -> assetPhase.getAsset().getId()));

    appointments.forEach(appointment -> {
      var asset = appointment.getAsset();
      var assetPhases = assetPhasesByAssetId.getOrDefault(asset.getId(), Collections.emptyList());

      publishAppointmentConfirmedSnsMessage(appointment, assetPhases, correlationId);
    });
  }

  void publishAppointmentConfirmedSnsMessage(Appointment appointment) {
    var assetPhases = assetPhaseRepository.findByAsset_Id(appointment.getAsset().getId());
    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    publishAppointmentConfirmedSnsMessage(appointment, assetPhases, correlationId);
  }

  void publishAppointmentConfirmedSnsMessage(
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
            correlationId
        )
    );
  }
}
