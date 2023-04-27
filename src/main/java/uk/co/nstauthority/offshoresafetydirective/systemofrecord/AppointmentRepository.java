package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AppointmentRepository extends CrudRepository<Appointment, UUID> {

  List<Appointment> findAllByAssetInAndResponsibleToDateIsNull(Collection<Asset> asset);

  List<Appointment> findAllByAsset_id(UUID assetId);

  List<Appointment> findAllByCreatedByNominationId(int createdByNominationId);

}
