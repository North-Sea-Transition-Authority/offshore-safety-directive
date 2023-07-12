package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentUpdateService {

  private final AppointmentRepository appointmentRepository;

  @Autowired
  public AppointmentUpdateService(AppointmentRepository appointmentRepository) {
    this.appointmentRepository = appointmentRepository;
  }

  @Transactional
  public void updateAppointment(AppointmentDto appointmentDto) {
    var appointment = appointmentRepository.findById(appointmentDto.appointmentId().id())
        .orElseThrow(() -> new IllegalStateException("No appointment found with id [%s]".formatted(
            appointmentDto.appointmentId().id()
        )));
    var operatorId = Integer.valueOf(appointmentDto.appointedOperatorId().id());
    appointment.setAppointedPortalOperatorId(operatorId);
    appointment.setAppointmentType(appointmentDto.appointmentType());
    appointment.setResponsibleFromDate(appointmentDto.appointmentFromDate().value());

    var toDate = Optional.ofNullable(appointmentDto.appointmentToDate())
        .map(AppointmentToDate::value)
        .orElse(null);

    appointment.setResponsibleToDate(toDate);
    appointmentRepository.save(appointment);
  }

}
