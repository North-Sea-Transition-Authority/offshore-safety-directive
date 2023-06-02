package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;

@Service
class AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final NomineeDetailAccessService nomineeDetailAccessService;

  private final Clock clock;

  @Autowired
  AppointmentService(AppointmentRepository appointmentRepository,
                     NomineeDetailAccessService nomineeDetailAccessService,
                     Clock clock) {
    this.appointmentRepository = appointmentRepository;
    this.nomineeDetailAccessService = nomineeDetailAccessService;
    this.clock = clock;
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
      appointment.setAppointmentType(AppointmentType.NOMINATED);
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
