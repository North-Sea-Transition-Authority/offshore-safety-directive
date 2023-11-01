package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
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
  private final AppointmentRemovedEventPublisher appointmentRemovedEventPublisher;
  private final AppointmentAddedEventPublisher appointmentAddedEventPublisher;

  private final AppointmentCorrectionService appointmentCorrectionService;

  @Autowired
  AppointmentService(AppointmentRepository appointmentRepository,
                     NomineeDetailAccessService nomineeDetailAccessService,
                     AssetRepository assetRepository,
                     Clock clock,
                     AppointmentRemovedEventPublisher appointmentRemovedEventPublisher,
                     AppointmentAddedEventPublisher appointmentAddedEventPublisher,
                     AppointmentCorrectionService appointmentCorrectionService) {
    this.appointmentRepository = appointmentRepository;
    this.nomineeDetailAccessService = nomineeDetailAccessService;
    this.assetRepository = assetRepository;
    this.clock = clock;
    this.appointmentRemovedEventPublisher = appointmentRemovedEventPublisher;
    this.appointmentAddedEventPublisher = appointmentAddedEventPublisher;
    this.appointmentCorrectionService = appointmentCorrectionService;
  }

  @Transactional
  public void addManualAppointment(AppointmentCorrectionForm form, AssetDto assetDto) {

    var savedAppointment = appointmentCorrectionService.applyCorrectionToAppointment(form, assetDto, new Appointment());
    appointmentAddedEventPublisher.publish(new AppointmentId(savedAppointment.getId()));
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

        var forwardApprovedAppointments = removeLinkBetweenAppointmentAndCreatedByAppointments(appointment);
        if (!forwardApprovedAppointments.isEmpty()) {
          appointmentsToSave.addAll(forwardApprovedAppointments);
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
