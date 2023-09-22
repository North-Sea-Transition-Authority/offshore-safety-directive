package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionForm;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;

@Service
public class AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final NomineeDetailAccessService nomineeDetailAccessService;
  private final AssetRepository assetRepository;
  private final Clock clock;
  private final AppointmentCorrectionService appointmentCorrectionService;

  @Autowired
  AppointmentService(AppointmentRepository appointmentRepository,
                     NomineeDetailAccessService nomineeDetailAccessService,
                     AssetRepository assetRepository,
                     Clock clock, AppointmentCorrectionService appointmentCorrectionService) {
    this.appointmentRepository = appointmentRepository;
    this.nomineeDetailAccessService = nomineeDetailAccessService;
    this.assetRepository = assetRepository;
    this.clock = clock;
    this.appointmentCorrectionService = appointmentCorrectionService;
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

    var savedAppointment = appointmentRepository.save(appointment);

    appointmentCorrectionService.updateCorrection(savedAppointment, form);
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
      newAppointments.add(appointment);
    });

    var appointmentsToSave = Stream.concat(endedAppointments.stream(), newAppointments.stream()).toList();

    appointmentRepository.saveAll(appointmentsToSave);
    return newAppointments;
  }

  private List<Appointment> endExistingAppointments(Collection<Asset> assets, LocalDate endDate) {
    var existingAppointments = appointmentRepository.findAllByAssetInAndResponsibleToDateIsNull(assets);
    if (!existingAppointments.isEmpty()) {
      existingAppointments.forEach(appointment -> appointment.setResponsibleToDate(endDate));
    }
    return existingAppointments;
  }

}
