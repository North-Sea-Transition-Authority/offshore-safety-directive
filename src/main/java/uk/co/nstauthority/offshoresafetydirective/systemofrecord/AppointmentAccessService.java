package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppointmentAccessService {

  public static final Set<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(
      AppointmentStatus.EXTANT,
      AppointmentStatus.TERMINATED
  );

  private final AppointmentRepository appointmentRepository;

  @Autowired
  public AppointmentAccessService(AppointmentRepository appointmentRepository) {
    this.appointmentRepository = appointmentRepository;
  }

  public List<AppointmentDto> getActiveAppointmentDtosForAsset(AssetId assetId) {
    return appointmentRepository.findAllByAsset_idAndAppointmentStatusIn(assetId.id(), ACTIVE_STATUSES)
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

  public Optional<Appointment> getAppointmentByStatus(AppointmentId appointmentId, AppointmentStatus appointmentStatus) {
    return appointmentRepository.findByIdAndAppointmentStatus(appointmentId.id(), appointmentStatus);
  }

  public List<Appointment> getActiveAppointmentsForAsset(AssetId assetId) {
    return appointmentRepository.findAllByAsset_idAndAppointmentStatusIn(assetId.id(), ACTIVE_STATUSES);
  }

  public List<Appointment> getAppointmentsForAssets(
      Collection<AppointmentStatus> statuses,
      List<String> portalAssetIds,
      PortalAssetType portalAssetType) {
    return appointmentRepository.findAppointmentsByAppointmentStatusInAndAsset_PortalAssetIdInAndAsset_PortalAssetType(
        statuses,
        portalAssetIds,
        portalAssetType
    );
  }
}
