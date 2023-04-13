package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.AppointmentCreatedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;

@Service
class AppointmentSnsService {

  static final String APPOINTMENTS_TOPIC_NAME = "osd-appointments";

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
    appointmentsTopicArn = snsService.getOrCreateTopic(APPOINTMENTS_TOPIC_NAME);
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
    });
  }
}
