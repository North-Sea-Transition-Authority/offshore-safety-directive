package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.WonsGeologicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAddedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentRemovedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Asset;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhaseDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhasePersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
class WonsNotificationCompletedService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WonsNotificationCompletedService.class);
  private final AssetAccessService assetAccessService;
  private final AssetPersistenceService assetPersistenceService;
  private final AppointmentService appointmentService;
  private final AppointmentAccessService appointmentAccessService;
  private final AssetPhasePersistenceService assetPhasePersistenceService;
  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private final Clock clock;
  private final AppointmentAddedEventPublisher appointmentAddedEventPublisher;
  private final AppointmentRemovedEventPublisher appointmentRemovedEventPublisher;

  WonsNotificationCompletedService(AssetAccessService assetAccessService,
                                   AssetPersistenceService assetPersistenceService,
                                   AppointmentService appointmentService,
                                   AppointmentAccessService appointmentAccessService,
                                   AssetPhasePersistenceService assetPhasePersistenceService,
                                   AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                                   Clock clock, AppointmentAddedEventPublisher appointmentAddedEventPublisher,
                                   AppointmentRemovedEventPublisher appointmentRemovedEventPublisher) {
    this.assetAccessService = assetAccessService;
    this.assetPersistenceService = assetPersistenceService;
    this.appointmentService = appointmentService;
    this.appointmentAccessService = appointmentAccessService;
    this.assetPhasePersistenceService = assetPhasePersistenceService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.clock = clock;
    this.appointmentAddedEventPublisher = appointmentAddedEventPublisher;
    this.appointmentRemovedEventPublisher = appointmentRemovedEventPublisher;
  }

  @Transactional
  public void processParentWellboreNotification(WonsGeologicalSidetrackNotificationCompletedEpmqMessage message) {
    processParentWellboreNotification(
        message.getNotificationId(),
        message.getCreatedWellboreId(),
        message.getSubmittedOnWellboreId(),
        message.isUsingParentWellboreAppointment()
    );
  }

  @Transactional
  public void processParentWellboreNotification(String notificationId, Integer targetWellboreId,
                                                Integer parentWellboreId, boolean usingParentWellboreAppointment) {
    if (!usingParentWellboreAppointment) {
      LOGGER.info("""
              Wellbore {} resulting from geological sidetrack notification {} is not using the parent wellbore appointment.
              No appointment will be created.
              """,
          targetWellboreId,
          notificationId
      );
      return;
    }
    var optionalAppointment = assetAccessService.getExtantAsset(
            new PortalAssetId(parentWellboreId.toString()),
            PortalAssetType.WELLBORE
        )
        .flatMap(assetDto -> appointmentAccessService.getCurrentAppointmentForAsset(assetDto.assetId()));

    if (optionalAppointment.isEmpty()) {
      LOGGER.info(
          "The parent wellbore {} of wellbore {} resulting from geological sidetrack notification {} " +
              "does not have an active appointment.\n" + "No appointment for wellbore {} will be created.",
          parentWellboreId,
          targetWellboreId,
          notificationId,
          targetWellboreId
      );
      return;
    }
    var parentAppointment = optionalAppointment.get();

    Asset childAsset;
    try {
      childAsset = assetPersistenceService.getOrCreateAsset(
          new PortalAssetId(targetWellboreId.toString()),
          PortalAssetType.WELLBORE
      );
    } catch (Exception e) {
      LOGGER.error("Unable to create asset for child appointment {}", targetWellboreId, e);
      return;
    }

    var childWellAppointment = createAppointmentFromParentWellAppointment(
        childAsset,
        parentAppointment
    );

    linkAssetPhases(parentAppointment, childWellAppointment);

    appointmentAddedEventPublisher.publish(new AppointmentId(childWellAppointment.getId()));

    LOGGER.info("""
            Created appointment for wellbore {} with parent wellbore {} resulting from geological sidetrack notification {}
            """,
        targetWellboreId,
        parentWellboreId,
        notificationId
    );
  }

  private Appointment createAppointmentFromParentWellAppointment(Asset childAsset,
                                                                 Appointment parentAppointment) {

    var currentAppointmentForAsset =
        appointmentAccessService.getCurrentAppointmentForAsset(new AssetId(childAsset.getId()));

    var childAppointment = appointmentService.createAppointmentLinkedToOtherAppointment(
        parentAppointment,
        childAsset,
        LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()),
        AppointmentType.PARENT_WELLBORE,
        parentAppointment.getAppointedPortalOperatorId(),
        AppointmentStatus.EXTANT
    );

    currentAppointmentForAsset.ifPresent(appointment -> {
      appointmentService.endAppointment(
          appointment,
          LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault())
      );
      appointmentRemovedEventPublisher.publish(new AppointmentId(appointment.getId()));
    });

    return childAppointment;
  }

  private void linkAssetPhases(Appointment parentWellAppointment, Appointment childWellAppointment) {
    var phaseNames = assetAppointmentPhaseAccessService.getPhasesByAppointment(parentWellAppointment)
        .stream()
        .map(AssetAppointmentPhase::value)
        .toList();

    var assetPhase = new AssetPhaseDto(
        childWellAppointment.getAsset(),
        childWellAppointment,
        phaseNames
    );
    assetPhasePersistenceService.createAssetPhases(Set.of(assetPhase));
  }

}
