package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends CrudRepository<Appointment, UUID> {

  List<Appointment> findAllByAssetInAndResponsibleToDateIsNullAndAppointmentStatusIn(Collection<Asset> asset,
                                                                                     Collection<AppointmentStatus> statuses);

  List<Appointment> findAllByAsset_idAndAppointmentStatusIn(UUID assetId, Collection<AppointmentStatus> statuses);

  List<Appointment> findAllByCreatedByNominationId(UUID createdByNominationId);

  List<Appointment> findAllByCreatedByAppointmentId(UUID appointmentId);

  List<Appointment> findAppointmentsByAppointmentStatusInAndAsset_PortalAssetIdInAndAsset_PortalAssetType(
      Collection<AppointmentStatus> appointmentStatus,
      List<String> portalAssetId,
      PortalAssetType portalAssetType
  );

  Optional<Appointment> findByIdAndAppointmentStatus(UUID appointmentId, AppointmentStatus appointmentStatus);

  List<Appointment> findAllByAsset_PortalAssetIdInAndAppointmentStatus(Collection<String> assetIds,
                                                                       AppointmentStatus appointmentStatus);

}
