package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionForm;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;

@Service
public class AppointmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentService.class);
  private final AppointmentRepository appointmentRepository;
  private final NomineeDetailAccessService nomineeDetailAccessService;
  private final AssetRepository assetRepository;
  private final Clock clock;
  private final AppointmentCorrectionService appointmentCorrectionService;
  private final AppointmentRemovedEventPublisher appointmentRemovedEventPublisher;
  private final AppointmentAddedEventPublisher appointmentAddedEventPublisher;


  @Autowired
  AppointmentService(AppointmentRepository appointmentRepository,
                     NomineeDetailAccessService nomineeDetailAccessService,
                     AssetRepository assetRepository,
                     Clock clock, AppointmentCorrectionService appointmentCorrectionService,
                     AppointmentRemovedEventPublisher appointmentRemovedEventPublisher,
                     AppointmentAddedEventPublisher appointmentAddedEventPublisher) {
    this.appointmentRepository = appointmentRepository;
    this.nomineeDetailAccessService = nomineeDetailAccessService;
    this.assetRepository = assetRepository;
    this.clock = clock;
    this.appointmentCorrectionService = appointmentCorrectionService;
    this.appointmentRemovedEventPublisher = appointmentRemovedEventPublisher;
    this.appointmentAddedEventPublisher = appointmentAddedEventPublisher;
  }

  @Transactional
  public void addManualAppointment(AppointmentCorrectionForm form, AssetDto assetDto) {

    var createdByNominationId = Optional.ofNullable(form.getOnlineNominationReference())
        .map(UUID::fromString)
        .orElse(null);

    var assetId = Optional.ofNullable(assetDto.portalAssetId())
        .map(PortalAssetId::id)
        .orElseThrow(() -> new IllegalStateException("No ID found for AssetDto"));

    var asset = assetRepository.findByPortalAssetIdAndPortalAssetType(
            assetId,
            assetDto.portalAssetType()
        )
        .orElseThrow(() -> new IllegalStateException(
            "No Asset with ID [%s] found for manual appointment creation".formatted(
                assetDto.portalAssetId()
            )
        ));

    // TODO OSDOP-583 - Read below block for details
    /*
    The block below creates an appointment as the correction service expects an appointment to exist within the database.
    We are calling the correction service as logic to persist data on the form is already handled within this method.

    The action to take is to investigate refactoring this work to be more streamlined.
     */
    var appointment = new Appointment();
    appointment.setAsset(asset);
    appointment.setAppointedPortalOperatorId(form.getAppointedOperatorId());
    appointment.setResponsibleFromDate(LocalDate.now());
    appointment.setResponsibleToDate(LocalDate.now());
    appointment.setAppointmentType(AppointmentType.valueOf(form.getAppointmentType()));
    appointment.setCreatedByNominationId(createdByNominationId);
    appointment.setCreatedByLegacyNominationReference(form.getOfflineNominationReference().getInputValue());
    appointment.setCreatedByAppointmentId(null);
    appointment.setCreatedDatetime(clock.instant());
    appointment.setAppointmentStatus(AppointmentStatus.EXTANT);

    var savedAppointment = appointmentRepository.save(appointment);
    appointmentCorrectionService.saveAppointment(savedAppointment, form);
    appointmentAddedEventPublisher.publish(new AppointmentId(savedAppointment.getId()));
  }

  @Transactional
  public List<Appointment> addAppointments(NominationDetail nominationDetail, LocalDate appointmentConfirmationDate,
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
        appointment.setAppointmentStatus(AppointmentStatus.REMOVED);
        appointmentRepository.save(appointment);

        appointmentRemovedEventPublisher.publish(new AppointmentId(appointment.getId()));
      }
    }
  }

  @Transactional
  public void endAppointmentsForSubareas(Collection<LicenceBlockSubareaDto> subareaDtos) {
    // TODO OSDOP-557 - End forward approval appointments for the given subarea
  }

  @Transactional
  public void endAppointmentsForSubareas(Collection<LicenceBlockSubareaDto> subareaDtos, String correctionId) {
    var portalAssetIds = subareaDtos.stream()
        .map(licenceBlockSubareaDto -> licenceBlockSubareaDto.subareaId().id())
        .toList();

    List<Asset> assets = assetRepository.findByPortalAssetIdInAndPortalAssetType(
        portalAssetIds,
        PortalAssetType.SUBAREA
    );

    var existingAppointments = endExistingAppointments(assets, LocalDate.now());

    if (!existingAppointments.isEmpty()) {
      appointmentRepository.saveAll(existingAppointments);
    }

    if (!assets.isEmpty()) {
      assets.forEach(asset -> removeAsset(asset, correctionId));
      assetRepository.saveAll(assets);
    }
  }

  private void removeAsset(Asset asset, String correctionId) {
    switch (asset.getStatus()) {
      case REMOVED -> LOGGER.warn(
          "Attempting to remove asset with ID [%s] but asset already has a non-extant status".formatted(asset.getId())
      );
      case EXTANT -> {
        asset.setStatus(AssetStatus.REMOVED);
        asset.setPortalEventId(correctionId);
        asset.setPortalEventType(PortalEventType.PEARS_CORRECTION);
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
      existingAppointments.forEach(appointment -> appointment.setResponsibleToDate(endDate));
    }
    return existingAppointments;
  }

}
