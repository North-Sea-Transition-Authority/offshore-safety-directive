package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionForm;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.message.ended.AppointmentEndedEventPublisher;

@Service
public class AppointmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentService.class);
  static final RequestPurpose SEARCH_SUBAREAS_BY_LICENCE_REFERENCE_PURPOSE =
      new RequestPurpose("Find non-extant subareas by licence reference to end appointments in system of record");

  private final AppointmentRepository appointmentRepository;
  private final NomineeDetailAccessService nomineeDetailAccessService;
  private final AssetRepository assetRepository;
  private final Clock clock;
  private final AppointmentRemovedEventPublisher appointmentRemovedEventPublisher;
  private final AppointmentAddedEventPublisher appointmentAddedEventPublisher;
  private final AppointmentCorrectionService appointmentCorrectionService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;
  private final AppointmentEndedEventPublisher appointmentEndedEventPublisher;

  @Autowired
  AppointmentService(AppointmentRepository appointmentRepository,
                     NomineeDetailAccessService nomineeDetailAccessService,
                     AssetRepository assetRepository,
                     Clock clock,
                     AppointmentRemovedEventPublisher appointmentRemovedEventPublisher,
                     AppointmentAddedEventPublisher appointmentAddedEventPublisher,
                     AppointmentCorrectionService appointmentCorrectionService,
                     LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                     AppointmentEndedEventPublisher appointmentEndedEventPublisher) {
    this.appointmentRepository = appointmentRepository;
    this.nomineeDetailAccessService = nomineeDetailAccessService;
    this.assetRepository = assetRepository;
    this.clock = clock;
    this.appointmentRemovedEventPublisher = appointmentRemovedEventPublisher;
    this.appointmentAddedEventPublisher = appointmentAddedEventPublisher;
    this.appointmentCorrectionService = appointmentCorrectionService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
    this.appointmentEndedEventPublisher = appointmentEndedEventPublisher;
  }

  @Transactional
  public void addManualAppointment(AppointmentCorrectionForm form, AssetDto assetDto) {

    var savedAppointment = appointmentCorrectionService.applyCorrectionToAppointment(form, assetDto, new Appointment());
    appointmentAddedEventPublisher.publish(new AppointmentId(savedAppointment.getId()));
  }

  @Transactional
  public Appointment createAppointmentLinkedToOtherAppointment(Appointment linkedAppointment, Asset asset,
                                                               LocalDate responsibleFromDate,
                                                               AppointmentType appointmentType, Integer operatorId,
                                                               AppointmentStatus appointmentStatus) {
    var appointment = new Appointment();
    appointment.setAsset(asset);
    appointment.setResponsibleFromDate(responsibleFromDate);
    appointment.setCreatedByAppointmentId(linkedAppointment.getId());
    appointment.setAppointmentType(appointmentType);
    appointment.setAppointedPortalOperatorId(operatorId);
    appointment.setCreatedDatetime(clock.instant());
    appointment.setAppointmentStatus(appointmentStatus);
    return appointmentRepository.save(appointment);
  }

  @Transactional
  public List<Appointment> createAppointmentsFromNomination(NominationDetail nominationDetail,
                                                            LocalDate appointmentConfirmationDate,
                                                            Collection<Asset> assets) {

    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var nomineeDetailDto = nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to get NomineeDetailDto for NominationDetail [%s]".formatted(
                nominationDetailDto.nominationDetailId()
            )));

    var endedAppointments = endExistingAppointments(assets, appointmentConfirmationDate);

    var newAppointments = new ArrayList<Appointment>();

    assets.forEach(asset -> {
      var appointment = new Appointment();
      appointment.setAsset(asset);
      appointment.setResponsibleFromDate(appointmentConfirmationDate);
      appointment.setCreatedByNominationId(nominationDetail.getNomination().getId());
      appointment.setAppointmentType(AppointmentType.ONLINE_NOMINATION);
      appointment.setAppointedPortalOperatorId(nomineeDetailDto.nominatedOrganisationId().id());
      appointment.setCreatedDatetime(clock.instant());
      appointment.setAppointmentStatus(AppointmentStatus.EXTANT);
      newAppointments.add(appointment);
    });

    var appointmentsToSave = Stream.concat(endedAppointments.stream(), newAppointments.stream()).toList();

    appointmentRepository.saveAll(appointmentsToSave);
    return newAppointments;
  }

  @Transactional
  public void removeAppointment(Appointment appointment) {
    switch (appointment.getAppointmentStatus()) {
      case REMOVED -> throw new IllegalStateException(
          "Appointment with ID [%s] has already been removed".formatted(appointment.getId())
      );
      case TERMINATED -> throw new IllegalStateException(
          "Appointment with ID [%s] cannot be removed as it has been terminated".formatted(appointment.getId())
      );
      case EXTANT -> {
        List<Appointment> appointmentsToSave = new ArrayList<>();

        var appointmentsWithRemovedLinks = removeLinkBetweenAppointmentAndCreatedByAppointments(appointment);
        if (!appointmentsWithRemovedLinks.isEmpty()) {
          appointmentsToSave.addAll(appointmentsWithRemovedLinks);
        }

        appointment.setAppointmentStatus(AppointmentStatus.REMOVED);
        appointmentsToSave.add(appointment);

        appointmentRepository.saveAll(appointmentsToSave);
        appointmentRemovedEventPublisher.publish(new AppointmentId(appointment.getId()));
      }
    }
  }

  private List<Appointment> removeLinkBetweenAppointmentAndCreatedByAppointments(Appointment appointment) {
    var linkedAppointments = appointmentRepository.findAllByCreatedByAppointmentId(appointment.getId());
    var appointmentsWithCreatedByRemoved = new ArrayList<Appointment>();

    linkedAppointments.forEach(createdByAppointment -> {
      createdByAppointment.setCreatedByAppointmentId(null);
      appointmentsWithCreatedByRemoved.add(createdByAppointment);
    });

    return appointmentsWithCreatedByRemoved;
  }

  @Transactional
  public void endAppointmentsForNonExtantSubareasWithLicenceReference(String licenceReference, String eventId,
                                                                      PortalEventType portalEventType) {
    List<LicenceBlockSubareaDto> nonExtantSubareas =
        licenceBlockSubareaQueryService.searchSubareasByLicenceReferenceWithStatuses(
            licenceReference,
            List.of(SubareaStatus.NOT_EXTANT),
            SEARCH_SUBAREAS_BY_LICENCE_REFERENCE_PURPOSE
        );

    if (!nonExtantSubareas.isEmpty()) {
      this.endAppointmentsForSubareas(nonExtantSubareas, eventId, portalEventType);
    }
  }

  @Transactional
  public void endAppointmentsForSubareas(Collection<LicenceBlockSubareaDto> subareaDtos, String eventId,
                                         PortalEventType portalEventType) {
    var portalAssetIds = subareaDtos.stream()
        .map(licenceBlockSubareaDto -> licenceBlockSubareaDto.subareaId().id())
        .toList();

    List<Asset> assets = assetRepository.findByPortalAssetIdInAndPortalAssetTypeAndStatusIs(
        portalAssetIds,
        PortalAssetType.SUBAREA,
        AssetStatus.EXTANT);

    var existingAppointments = endExistingAppointments(assets, LocalDate.now());

    if (!existingAppointments.isEmpty()) {
      appointmentRepository.saveAll(existingAppointments);
    }

    if (!assets.isEmpty()) {
      assets.forEach(asset -> removeAsset(asset, eventId, portalEventType));
      assetRepository.saveAll(assets);
    }
  }

  public Set<AssetDto> getAssetsWithActiveAppointments(Collection<PortalAssetId> portalAssetIds,
                                                       PortalAssetType portalAssetType) {

    var assetIds = portalAssetIds.stream()
        .map(PortalAssetId::id)
        .toList();
    return appointmentRepository.findAllByAsset_PortalAssetIdInAndAppointmentStatus(
            assetIds,
            AppointmentStatus.EXTANT
        )
        .stream()
        .filter(appointment -> Objects.equals(appointment.getAsset().getPortalAssetType(), portalAssetType))
        .filter(appointment -> Objects.isNull(appointment.getResponsibleToDate()))
        .map(appointment -> AssetDto.fromAsset(appointment.getAsset()))
        .collect(Collectors.toSet());
  }

  private void removeAsset(Asset asset, String eventId, PortalEventType portalEventType) {
    switch (asset.getStatus()) {
      case REMOVED -> LOGGER.warn(
          "Attempting to remove asset with ID [%s] but asset already has a non-extant status".formatted(asset.getId())
      );
      case EXTANT -> {
        asset.setStatus(AssetStatus.REMOVED);
        asset.setPortalEventId(eventId);
        asset.setPortalEventType(portalEventType);
      }
    }
  }

  private List<Appointment> endExistingAppointments(Collection<Asset> assets, LocalDate endDate) {
    var existingAppointments =
        appointmentRepository.findAllByAssetInAndResponsibleToDateIsNullAndAppointmentStatusIn(
            assets,
            EnumSet.of(AppointmentStatus.EXTANT)
        );
    if (!existingAppointments.isEmpty()) {
      existingAppointments.forEach(appointment -> {
        appointment.setResponsibleToDate(endDate);
        appointmentEndedEventPublisher.publish(appointment.getId());
      });
    }
    return existingAppointments;
  }

  @Transactional
  public void setAppointmentStatus(Appointment appointment, AppointmentStatus appointmentStatus) {
    appointment.setAppointmentStatus(appointmentStatus);
    appointmentRepository.save(appointment);
  }

  @Transactional
  public void endAppointment(Appointment appointment, LocalDate endDate) {
    appointment.setResponsibleToDate(endDate);
    appointmentRepository.save(appointment);
  }
}
