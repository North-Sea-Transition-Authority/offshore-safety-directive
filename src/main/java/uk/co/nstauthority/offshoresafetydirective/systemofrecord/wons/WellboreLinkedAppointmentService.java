package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAddedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Asset;
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
public class WellboreLinkedAppointmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WellboreLinkedAppointmentService.class);

  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private final AssetPersistenceService assetPersistenceService;
  private final AppointmentAccessService appointmentAccessService;
  private final AppointmentService appointmentService;
  private final AppointmentEndedEventPublisher appointmentEndedEventPublisher;
  private final Clock clock;
  private final AppointmentAddedEventPublisher appointmentAddedEventPublisher;
  private final AssetPhasePersistenceService assetPhasePersistenceService;

  @Autowired
  public WellboreLinkedAppointmentService(AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                                          AssetPersistenceService assetPersistenceService,
                                          AppointmentAccessService appointmentAccessService,
                                          AppointmentService appointmentService,
                                          AppointmentEndedEventPublisher appointmentEndedEventPublisher,
                                          Clock clock, AppointmentAddedEventPublisher appointmentAddedEventPublisher,
                                          AssetPhasePersistenceService assetPhasePersistenceService) {
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.assetPersistenceService = assetPersistenceService;
    this.appointmentAccessService = appointmentAccessService;
    this.appointmentService = appointmentService;
    this.appointmentEndedEventPublisher = appointmentEndedEventPublisher;
    this.clock = clock;
    this.appointmentAddedEventPublisher = appointmentAddedEventPublisher;
    this.assetPhasePersistenceService = assetPhasePersistenceService;
  }

  public boolean appointmentDoesNotCoverWellboreIntent(WellDto wellDto, Appointment appointmentToLinkTo) {
    return !doesAppointmentCoverWellboreIntent(wellDto, appointmentToLinkTo);
  }

  private boolean doesAppointmentCoverWellboreIntent(WellDto wellbore, Appointment appointmentToLinkTo) {

    Set<WellPhase> appointmentPhases = assetAppointmentPhaseAccessService.getPhasesByAppointment(appointmentToLinkTo)
        .stream()
        .map(phase -> WellPhase.valueOfOrNull(phase.value()))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    return switch (wellbore.intent()) {
      case EXPLORATION, APPRAISAL -> appointmentPhases.contains(WellPhase.EXPLORATION_AND_APPRAISAL);
      case DEVELOPMENT -> appointmentPhases.contains(WellPhase.DEVELOPMENT);
      // This is a historical intent in WONS which doesn't map to an appointment phase in this service.
      // If a wellbore has this intent the appointment can never cover the wellbore intent so just return false
      case CARBON_CAPTURE_AND_STORAGE -> false;
    };
  }

  public void createLinkedWellboreAppointment(WellDto wellbore, Appointment appointmentToLinkTo,
                                              AppointmentType appointmentTypeOfNewAppointment) {

    Asset wellboreAsset;
    Integer wellboreId = wellbore.wellboreId().id();

    try {
      wellboreAsset = assetPersistenceService.getOrCreateAsset(
          new PortalAssetId(wellboreId.toString()),
          PortalAssetType.WELLBORE
      );
    } catch (Exception e) {
      LOGGER.error("Unable to create asset for wellbore with ID {}", wellboreId, e);
      return;
    }

    var resultingAppointment = createAppointmentFromLinkedAppointment(
        wellboreAsset,
        appointmentToLinkTo,
        appointmentTypeOfNewAppointment
    );

    linkAssetPhases(appointmentToLinkTo, resultingAppointment);

    appointmentAddedEventPublisher.publish(new AppointmentId(resultingAppointment.getId()));

    LOGGER.info(
        "Created appointment {} for wellbore {} from link appointment {}",
        resultingAppointment.getId(),
        wellboreId,
        appointmentToLinkTo.getId()
    );
  }

  private Appointment createAppointmentFromLinkedAppointment(Asset wellboreAsset, Appointment appointmentToLinkTo,
                                                             AppointmentType appointmentTypeOfNewAppointment) {

    var currentAppointmentForAsset =
        appointmentAccessService.getCurrentAppointmentForAsset(new AssetId(wellboreAsset.getId()));

    var childAppointment = appointmentService.createAppointmentLinkedToOtherAppointment(
        appointmentToLinkTo,
        wellboreAsset,
        LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()),
        appointmentTypeOfNewAppointment,
        appointmentToLinkTo.getAppointedPortalOperatorId(),
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

  private void linkAssetPhases(Appointment sourceAppointment, Appointment destinationAppointment) {

    var phaseNames = assetAppointmentPhaseAccessService.getPhasesByAppointment(sourceAppointment)
        .stream()
        .map(AssetAppointmentPhase::value)
        .toList();

    var assetPhase = new AssetPhaseDto(
        destinationAppointment.getAsset(),
        destinationAppointment,
        phaseNames
    );

    assetPhasePersistenceService.createAssetPhases(Set.of(assetPhase));
  }
}
