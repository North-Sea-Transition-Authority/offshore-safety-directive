package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppointmentAccessService {

  private final AppointmentRepository appointmentRepository;

  @Autowired
  public AppointmentAccessService(AppointmentRepository appointmentRepository) {
    this.appointmentRepository = appointmentRepository;
  }

  public List<AppointmentDto> getAppointmentsForAsset(AssetId assetId) {
    return appointmentRepository.findAllByAsset_id(assetId.id())
        .stream()
        .map(AppointmentDto::fromAppointment)
        .toList();
  }

  public Optional<AppointmentDto> findAppointmentDtoById(AppointmentId appointmentId) {
    return appointmentRepository.findById(appointmentId.id())
        .map(AppointmentDto::fromAppointment);
  }

  public Optional<Appointment> getAppointment(AppointmentId appointmentId) {
    return appointmentRepository.findById(appointmentId.id());
  }
}
