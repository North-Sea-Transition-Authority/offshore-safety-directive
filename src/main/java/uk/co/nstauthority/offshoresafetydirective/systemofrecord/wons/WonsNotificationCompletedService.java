package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.geological.WonsGeologicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.mechanical.WonsMechicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.respud.WonsRespudNotificationCompletedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAddedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.message.ended.AppointmentEndedEventPublisher;

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
  private final AppointmentEndedEventPublisher appointmentEndedEventPublisher;
  private final WellQueryService wellQueryService;

  WonsNotificationCompletedService(AssetAccessService assetAccessService,
                                   AssetPersistenceService assetPersistenceService,
                                   AppointmentService appointmentService,
                                   AppointmentAccessService appointmentAccessService,
                                   AssetPhasePersistenceService assetPhasePersistenceService,
                                   AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                                   Clock clock, AppointmentAddedEventPublisher appointmentAddedEventPublisher,
                                   AppointmentEndedEventPublisher appointmentEndedEventPublisher,
                                   WellQueryService wellQueryService) {
    this.assetAccessService = assetAccessService;
    this.assetPersistenceService = assetPersistenceService;
    this.appointmentService = appointmentService;
    this.appointmentAccessService = appointmentAccessService;
    this.assetPhasePersistenceService = assetPhasePersistenceService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.clock = clock;
    this.appointmentAddedEventPublisher = appointmentAddedEventPublisher;
    this.appointmentEndedEventPublisher = appointmentEndedEventPublisher;
    this.wellQueryService = wellQueryService;
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
  public void processParentWellboreNotification(WonsMechicalSidetrackNotificationCompletedEpmqMessage message) {
    processParentWellboreNotification(
        message.getNotificationId(),
        message.getCreatedWellboreId(),
        message.getSubmittedOnWellboreId(),
        message.isUsingParentWellboreAppointment()
    );
  }

  @Transactional
  public void processParentWellboreNotification(WonsRespudNotificationCompletedEpmqMessage message) {
    processParentWellboreNotification(
        message.getNotificationId(),
        message.getCreatedWellboreId(),
        message.getSubmittedOnWellboreId(),
        message.isUsingParentWellboreAppointment()
    );
  }

  private void processParentWellboreNotification(String notificationId, Integer targetWellboreId,
                                                 Integer parentWellboreId, boolean usingParentWellboreAppointment) {
    if (!usingParentWellboreAppointment) {
      LOGGER.info(
          "Wellbore {} resulting from notification {} is not using the parent wellbore appointment. " +
              "No appointment will be created.",
          targetWellboreId,
          notificationId
      );
      return;
    }

    Optional<Appointment> parentAppointmentOptional = assetAccessService.getExtantAsset(
            new PortalAssetId(parentWellboreId.toString()),
            PortalAssetType.WELLBORE
        )
        .flatMap(assetDto -> appointmentAccessService.getCurrentAppointmentForAsset(assetDto.assetId()));

    if (parentAppointmentOptional.isEmpty()) {
      LOGGER.info(
          "The parent wellbore {} of wellbore {} resulting from notification {} " +
              "does not have an active appointment. No appointment for wellbore {} will be created.",
          parentWellboreId,
          targetWellboreId,
          notificationId,
          targetWellboreId
      );
      return;
    }

    Appointment parentAppointment = parentAppointmentOptional.get();

    // need to check the intent of the child well from EPA and see parent appointment covers those phases
    WellDto childWellbore = wellQueryService
        .getWell(new WellboreId(targetWellboreId), new RequestPurpose("Get target well from notification event"))
        .orElseThrow(() -> new IllegalStateException(
            "Unable to find target wellbore from WONS with ID %d".formatted(targetWellboreId)
        ));

    if (!doesAppointmentCoverWellboreIntent(childWellbore, parentAppointment)) {
      LOGGER.info("The parent wellbore appointment with ID {} does not cover the intent of the child wellbore with ID {}. " +
          "No appointment will be created for the child wellbore", parentAppointment.getId(), childWellbore.wellboreId().id());

      return;
    }

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

    var childAppointment = createAppointmentFromParentWellAppointment(
        childAsset,
        parentAppointment
    );

    linkAssetPhases(parentAppointment, childAppointment);

    appointmentAddedEventPublisher.publish(new AppointmentId(childAppointment.getId()));

    LOGGER.info(
        "Created appointment for wellbore {} from parent wellbore {} resulting from notification {}",
        targetWellboreId,
        parentWellboreId,
        notificationId
    );
  }

  private Appointment createAppointmentFromParentWellAppointment(Asset childAsset, Appointment parentAppointment) {

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
      appointmentEndedEventPublisher.publish(appointment.getId());
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

  private boolean doesAppointmentCoverWellboreIntent(WellDto childWellbore, Appointment parentAppointment) {

    Set<WellPhase> appointmentPhases = assetAppointmentPhaseAccessService.getPhasesByAppointment(parentAppointment)
        .stream()
        .map(phase -> WellPhase.valueOfOrNull(phase.value()))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    return switch (childWellbore.intent()) {
      case EXPLORATION, APPRAISAL -> appointmentPhases.contains(WellPhase.EXPLORATION_AND_APPRAISAL);
      case DEVELOPMENT -> appointmentPhases.contains(WellPhase.DEVELOPMENT);
      // This is a historical intent in WONS which doesn't map to an appointment phase in this service.
      // If a wellbore has this intent the appointment can never cover the wellbore intent so just return false
      case CARBON_CAPTURE_AND_STORAGE -> false;
    };
  }

}
