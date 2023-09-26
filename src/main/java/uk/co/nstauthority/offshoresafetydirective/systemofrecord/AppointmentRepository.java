package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AppointmentRepository extends CrudRepository<Appointment, UUID> {

  List<Appointment> findAllByAssetInAndResponsibleToDateIsNullAndAppointmentStatusIn(Collection<Asset> asset,
                                                                                     Collection<AppointmentStatus> statuses);

  List<Appointment> findAllByAsset_idAndAppointmentStatusIn(UUID assetId, Collection<AppointmentStatus> statuses);

  List<Appointment> findAllByCreatedByNominationId(UUID createdByNominationId);

}
